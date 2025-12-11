// backend/server.js
import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import http from "http";
import multer from "multer";
import path from "path";
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
import compatibilityRoutes from "./routes/compatibilityRoutes.js";
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

app.use(
  cors({
    origin: process.env.CORS_ORIGIN || "*",
    credentials: true,
  })
);
app.use(express.json());

//multer para manejar archivos en memoria
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 5 * 1024 * 1024 }, // 5 MB
});

// middleware simple de auth para rutas HTTP
function authMiddleware(req, res, next) {
  try {
    const header = req.headers.authorization || "";
    const token = header.startsWith("Bearer ") ? header.slice(7) : null;

    if (!token) {
      return res.status(401).json({ message: "Token no proporcionado" });
    }

    const payload = jwt.verify(token, process.env.JWT_SECRET);
    // mismo payload que se usa en el socket: debe tener id_usuario
    req.user = payload;
    next();
  } catch (err) {
    console.error("Error en authMiddleware:", err.message);
    return res.status(401).json({ message: "Token inválido" });
  }
}

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
app.use("/api/compatibility", compatibilityRoutes);

// Health check
app.get("/health", (_req, res) => res.json({ ok: true }));

// === NUEVO: endpoint para subir foto de perfil ===
// POST /api/profile/photo  (body: multipart/form-data con campo "photo")
app.post(
  "/api/profile/photo",
  authMiddleware,
  upload.single("photo"),
  async (req, res) => {
    try {
      const userFromToken = req.user;
      const userId = userFromToken?.id_usuario;

      if (!userId) {
        return res
          .status(400)
          .json({ message: "No se encontró id_usuario en el token" });
      }

      if (!req.file) {
        return res
          .status(400)
          .json({ message: "No se envió archivo en el campo 'photo'" });
      }

      const bucket = process.env.SUPABASE_STORAGE_BUCKET;
      if (!bucket) {
        return res
          .status(500)
          .json({ message: "Falta SUPABASE_STORAGE_BUCKET en .env" });
      }

      const ext = path.extname(req.file.originalname) || ".jpg";
      const filePath = `avatars/${userId}_${Date.now()}${ext}`;

      // Subir a Supabase Storage (bucket product_im)
      const { data, error } = await supabase.storage
        .from(bucket)
        .upload(filePath, req.file.buffer, {
          contentType: req.file.mimetype,
          upsert: true,
        });

      if (error) {
        console.error("Error subiendo a Supabase Storage:", error);
        return res.status(500).json({ message: "Error subiendo imagen" });
      }

      // Obtener URL pública
      const { data: publicData } = supabase.storage
        .from(bucket)
        .getPublicUrl(filePath);

      const publicUrl = publicData?.publicUrl;
      if (!publicUrl) {
        return res
          .status(500)
          .json({ message: "No se pudo obtener URL pública de la imagen" });
      }

      // Actualizar la tabla public.usuario, columna foto, PK id_usuario
      const { error: updateError } = await supabase
        .from("usuario")
        .update({ foto: publicUrl })
        .eq("id_usuario", userId);

      if (updateError) {
        console.error("Error actualizando foto en BD:", updateError);
        return res.status(500).json({ message: "Error actualizando perfil" });
      }

      return res.json({ foto: publicUrl });
    } catch (err) {
      console.error("Error en /api/profile/photo:", err);
      return res.status(500).json({ message: "Error interno al subir foto" });
    }
  }
);

// 🔌 Servidor HTTP + Socket.IO
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
      socket.handshake.headers?.authorization ||
      "";

    const token = header.startsWith("Bearer ") ? header.slice(7) : header;

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
  console.log("Socket conectado:", socket.user?.id_usuario);

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
   * Enviar mensaje
   * payload: { chatId: number, contenido: string }
   */
  socket.on("send_message", async ({ chatId, contenido }, callback) => {
    try {
      const userId = socket.user.id_usuario;

      // Validar que el chat existe y que el usuario pertenece a él
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

      const msg = await saveMessage({
        chatId,
        senderId: userId,
        contenido,
      });

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

      if (receptorError)
        console.error("Error buscando receptor:", receptorError);

      // Enviar Notificación Push (Lógica de Cuello)
      if (receptor?.fcm_token) {
        const notifBody =
          contenido.length > 50 ? contenido.slice(0, 47) + "..." : contenido;
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
                visibility: "public",
              },
            },
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
