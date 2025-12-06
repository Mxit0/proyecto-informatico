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
import { supabase } from "./lib/supabaseClient.js";

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
    const header = socket.handshake.auth?.token ||
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

// 🔁 Helpers con Supabase

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

// 🎧 Lógica de tiempo real
io.on("connection", (socket) => {
  console.log("✅ Socket conectado:", socket.user?.id_usuario);

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

      callback?.({ ok: true, message: msg });
    } catch (err) {
      console.error("Error en send_message:", err);
      callback?.({ ok: false, error: "No se pudo enviar el mensaje" });
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
