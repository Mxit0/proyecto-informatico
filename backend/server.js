// backend/server.js
import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import http from "http";
import { Server as SocketIOServer } from "socket.io";
import jwt from "jsonwebtoken";

import authRoutes from "./routes/authRoutes.js";
import productRoutes from "./routes/productRoutes.js";
import userRoutes from "./routes/userRoutes.js";
import chatRoutes from "./routes/chatRoutes.js";
import carroRoutes from "./routes/carroRoutes.js";
import orderRoutes from "./routes/orderRoutes.js";
import reviewRoutes from "./routes/reviewRoutes.js";
import { supabase } from "./lib/supabaseClient.js";
import { admin } from "./lib/firebaseAdmin.js"; 

dotenv.config();

const app = express();

app.use(
  cors({
    origin: process.env.CORS_ORIGIN || "*",
    credentials: true,
  })
);
app.use(express.json());

// Rutas HTTP normales
app.use("/api/auth", authRoutes);
app.use("/api/productos", productRoutes);
app.use("/usuarios", userRoutes);          
app.use("/api/chat", chatRoutes);
app.use("/api/carro", carroRoutes);
app.use("/api/orders", orderRoutes);
app.use("/api/reviews", reviewRoutes);

// Health check
app.get("/health", (_req, res) => res.json({ ok: true }));

// 🔌 Servidor HTTP + Socket.IO
const server = http.createServer(app);

const io = new SocketIOServer(server, {
  cors: {
    origin: process.env.CORS_ORIGIN || "*",
    methods: ["GET", "POST"],
  },
});

// Normalizar pareja de usuarios (para que no hayan 2 chats duplicados)
function normalizePair(a, b) {
  return a < b ? [a, b] : [b, a];
}

// Middleware de autenticación para sockets (mismo JWT que en HTTP)
io.use((socket, next) => {
  try {
    const header =
      socket.handshake.auth?.token ||
      (socket.handshake.headers?.authorization || "");

    const token = header.startsWith("Bearer ")
      ? header.slice(7)
      : header;

    if (!token) return next(new Error("No token"));

    const payload = jwt.verify(token, process.env.JWT_SECRET);
    // payload es el user que firmaste en authRoutes (incluye id_usuario, correo, etc.)
    socket.user = payload;
    next();
  } catch (err) {
    console.error("Error autenticando socket:", err.message);
    next(new Error("Auth error"));
  }
});

// Helpers con Supabase
async function getOrCreateChatBetween(userAId, userBId) {
  const [u1, u2] = normalizePair(userAId, userBId);

  const { data: existing, error: selectError } = await supabase
    .from("chat")
    .select("*")
    .eq("id_usuario1", u1)
    .eq("id_usuario2", u2)
    .maybeSingle();

  if (selectError) throw selectError;
  if (existing) return existing;

  const { data, error: insertError } = await supabase
    .from("chat")
    .insert([{ id_usuario1: u1, id_usuario2: u2 }])
    .select("*")
    .single();

  if (insertError) throw insertError;
  return data;
}

async function saveMessage({ chatId, senderId, contenido }) {
  const { data, error } = await supabase
    .from("mensaje")
    .insert([{ id_chat: chatId, id_remitente: senderId, contenido }])
    .select("*")
    .single();

  if (error) throw error;
  return data;
}

//  Lógica de tiempo real
io.on("connection", (socket) => {
  console.log(" Socket conectado:", socket.user?.id_usuario);

  /**
   * Abrir chat entre el usuario logueado y otro usuario (vendedor/comprador)
   * payload: { otherUserId: number }
   */
  socket.on("open_chat_with_user", async ({ otherUserId }, callback) => {
    try {
      const currentUserId = socket.user.id_usuario;
      if (!currentUserId || !otherUserId) {
        return callback?.({ ok: false, error: "Faltan usuarios" });
      }

      const chat = await getOrCreateChatBetween(currentUserId, otherUserId);
      const room = `chat_${chat.id}`;
      socket.join(room);

      callback?.({ ok: true, chat });
    } catch (err) {
      console.error("Error en open_chat_with_user:", err);
      callback?.({ ok: false, error: "No se pudo abrir el chat" });
    }
  });

  /**
   * Unirse explícitamente a un chat (para cargar historial y escuchar mensajes)
   * payload: { chatId: number }
   */
  socket.on("join_chat", ({ chatId }) => {
    const room = `chat_${chatId}`;
    socket.join(room);
  });

  /**
   * Enviar mensaje
   * payload: { chatId: number, contenido: string }
   *
   * Ahora:
   *  - Guarda el mensaje
   *  - Lo emite al room
   *  - Envía notificación FCM al receptor (si tiene fcm_token)
   */
  socket.on("send_message", async ({ chatId, contenido }, callback) => {
    try {
      const userId = socket.user.id_usuario;

      // 1. Validar que el chat existe y que el usuario pertenece
      const { data: chat, error: chatError } = await supabase
        .from("chat")
        .select("id, id_usuario1, id_usuario2")
        .eq("id", chatId)
        .maybeSingle();

      if (chatError) throw chatError;
      if (!chat) {
        return callback?.({ ok: false, error: "Chat no encontrado" });
      }

      if (chat.id_usuario1 !== userId && chat.id_usuario2 !== userId) {
        return callback?.({ ok: false, error: "No perteneces a este chat" });
      }

      // 2. Guardar mensaje en la BD
      const msg = await saveMessage({
        chatId,
        senderId: userId,
        contenido,
      });

      // 3. Emitir mensaje en tiempo real al room
      const room = `chat_${chatId}`;
      io.to(room).emit("new_message", msg);

      // 4. Determinar receptor
      const receptorId =
        chat.id_usuario1 === userId ? chat.id_usuario2 : chat.id_usuario1;

      // 5. Buscar al receptor (para obtener su fcm_token)
      const { data: receptor, error: receptorError } = await supabase
        .from("usuario")
        .select("id_usuario, nombre_usuario, fcm_token")
        .eq("id_usuario", receptorId)
        .maybeSingle();

      if (receptorError) {
        console.error("Error buscando receptor para notificación:", receptorError);
      }

      // 6. Enviar notificación FCM si el usuario tiene token
      if (receptor?.fcm_token) {
        const notifBody =
          contenido.length > 50 ? contenido.slice(0, 47) + "..." : contenido;

        const titulo =
          socket.user?.nombre_usuario
            ? `${socket.user.nombre_usuario} te escribió`
            : "Nuevo mensaje";

        try {
          await admin.messaging().send({
            token: receptor.fcm_token,
            notification: {
              title: titulo,
              body: notifBody,
            },
            data: {
              chatId: String(chatId),
              remitenteId: String(userId),
            },
          });

          console.log("📨 Notificación FCM enviada a usuario", receptorId);
        } catch (errFCM) {
          console.error("Error enviando notificación FCM:", errFCM);
        }
      } else {
        console.log("Usuario sin fcm_token, no se envía push:", receptorId);
      }

      // 7. Responder al cliente que envió el mensaje
      callback?.({ ok: true, message: msg });
    } catch (err) {
      console.error("Error en send_message:", err);
      callback?.({ ok: false, error: "No se pudo enviar el mensaje" });
    }
  });

  socket.on("mark_messages_read", async ({ chatId }) => {
    try {
      const userId = socket.user.id_usuario;

      const { error } = await supabase
        .from("mensaje")
        .update({ leido: true })
        .eq("id_chat", chatId)
        .neq("id_remitente", userId)
        .eq("leido", false);

      if (error) throw error;

      const room = `chat_${chatId}`;
      io.to(room).emit("messages_read_update", { chatId });
    } catch (err) {
      console.error("Error en mark_messages_read:", err);
    }
  });

  socket.on("disconnect", () => {
    console.log("🔌 Socket desconectado:", socket.user?.id_usuario);
  });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`API + Socket.IO escuchando en http://localhost:${PORT}`);
});
