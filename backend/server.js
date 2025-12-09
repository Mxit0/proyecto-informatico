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
import foroRoutes from "./routes/foroRoutes.js"; 

import { supabase } from "./lib/supabaseClient.js";
import { admin } from "./lib/firebaseAdmin.js"; // <-- Importante: Esto viene de Cuello (Notificaciones)

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
// AGREGADO: Habilitamos la ruta de foros en la API
app.use("/api/foros", foroRoutes); 

// Health check
app.get("/health", (_req, res) => res.json({ ok: true }));

// Servidor HTTP + Socket.IO
const server = http.createServer(app);

const io = new SocketIOServer(server, {
  cors: {
    origin: process.env.CORS_ORIGIN || "*",
    methods: ["GET", "POST"],
  },
});

// AGREGADO: Vital para que el Foro emita eventos sin estar conectado al socket directo
// Esto permite que cuando hagas un POST /api/foros/../publicaciones, el controlador use req.app.get('io')
app.set("io", io);

// Normalizar pareja de usuarios
function normalizePair(a, b) {
  return a < b ? [a, b] : [b, a];
}

// Middleware de autenticación para sockets
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
  console.log("Socket conectado:", socket.user?.id_usuario);

  /**
   * Abrir chat entre usuarios
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
   * Unirse a Chat O Foro
   * Ahora soporta unirse a foros ("foro_1") y chats privados ("chat_1")
   */
  socket.on("join_chat", (data) => {
    // Caso Foro (Joaquín)
    if (data.room) {
        socket.join(data.room);
        console.log(`Socket unido a sala: ${data.room}`);
    } 
    // Caso Chat Privado (Cuello)
    else if (data.chatId) {
        const room = `chat_${data.chatId}`;
        socket.join(room);
    }
  });

  /**
   * Enviar mensaje (Mantenemos la lógica de Cuello con FCM)
   */
  socket.on("send_message", async ({ chatId, contenido }, callback) => {
    try {
      const userId = socket.user.id_usuario;

      // Validar chat
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

      // Guardar mensaje
      const msg = await saveMessage({
        chatId,
        senderId: userId,
        contenido,
      });

      // Emitir mensaje al room
      const room = `chat_${chatId}`;
      io.to(room).emit("new_message", msg);

      // Determinar receptor
      const receptorId =
        chat.id_usuario1 === userId ? chat.id_usuario2 : chat.id_usuario1;

      // Buscar receptor y token FCM
      const { data: receptor, error: receptorError } = await supabase
        .from("usuario")
        .select("id_usuario, nombre_usuario, fcm_token")
        .eq("id_usuario", receptorId)
        .maybeSingle();

      if (receptorError) console.error("Error buscando receptor:", receptorError);

      // Enviar Notificación Push (Lógica de Cuello)
      if (receptor?.fcm_token) {
        const notifBody = contenido.length > 50 ? contenido.slice(0, 47) + "..." : contenido;
        const titulo = socket.user?.nombre_usuario 
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
            android: {
              priority: "high",
              notification: {
                channelId: "chat_channel",
                priority: "high",
                defaultSound: true,
                visibility: "public"
              }
            }
          });
          console.log("FCM enviada a", receptorId);
        } catch (errFCM) {
          console.error("Error enviando FCM:", errFCM);
        }
      }

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

      if (!error) {
        const room = `chat_${chatId}`;
        io.to(room).emit("messages_read_update", { chatId });
      }
    } catch (err) {
      console.error("Error en mark_messages_read:", err);
    }
  });

  socket.on("disconnect", () => {
    console.log("Socket desconectado:", socket.user?.id_usuario);
  });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`API + Socket.IO escuchando en http://localhost:${PORT}`);
});