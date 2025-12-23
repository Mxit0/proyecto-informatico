// backend/routes/chatRoutes.js
import express from "express";
import { supabase } from "../lib/supabaseClient.js";
import { requireAuth } from "../middleware/auth.js";

const router = express.Router();

function normalizePair(a, b) {
  return a < b ? [a, b] : [b, a];
}

/**
 * Obtener todos los chats donde participa el usuario
 */

// backend/routes/chatRoutes.js

router.get("/", requireAuth, async (req, res) => {
  const userId = req.user.id_usuario; 

  try {
    const { data: chats, error } = await supabase
      .from("chat")
      .select("id, id_usuario1, id_usuario2, fecha_creacion")
      .or(`id_usuario1.eq.${userId},id_usuario2.eq.${userId}`);

    if (error) throw error;

    const chatsCompleto = await Promise.all(chats.map(async (chat) => {
        const otherUserId = (chat.id_usuario1 == userId) ? chat.id_usuario2 : chat.id_usuario1;

        const { data: otherUser } = await supabase
            .from("usuario")
            .select("nombre_usuario, foto")
            .eq("id_usuario", otherUserId)
            .single();

        const { data: lastMsg } = await supabase
            .from("mensaje")
            .select("contenido, fecha_envio") 
            .eq("id_chat", chat.id)
            .order("fecha_envio", { ascending: false }) 
            .limit(1)
            .maybeSingle();

        return {
            id: String(chat.id),
            
            name: otherUser ? otherUser.nombre_usuario : "Usuario Desconocido",
            photoUrl: otherUser ? otherUser.foto : null,
            otherUserId: Number(otherUserId),
            lastMessage: lastMsg ? lastMsg.contenido : "Sin mensajes",
            lastMessageDate: lastMsg ? lastMsg.fecha_envio : chat.fecha_creacion
        };
    }));

    chatsCompleto.sort((a, b) => {
        const dateA = new Date(a.lastMessageDate);
        const dateB = new Date(b.lastMessageDate);
        return dateB - dateA; 
    });

    res.json({ ok: true, chats: chatsCompleto });

  } catch (err) {
    console.error("Error en GET /api/chat:", err);
    res.status(500).json({ ok: false, error: "Error al obtener chats" });
  }
});

/**
 *  Obtener mensajes de un chat específico
 */
router.get("/:chatId/mensajes", requireAuth, async (req, res) => {
  const userId = req.user.id_usuario;
  const chatId = Number(req.params.chatId);

  if (Number.isNaN(chatId)) {
    return res.status(400).json({ ok: false, error: "chatId inválido" });
  }

  try {
    
    const { data: chat, error: chatError } = await supabase
      .from("chat")
      .select("id, id_usuario1, id_usuario2")
      .eq("id", chatId)
      .maybeSingle();

    if (chatError) throw chatError;
    if (!chat) {
      return res.status(404).json({ ok: false, error: "Chat no encontrado" });
    }

    if (chat.id_usuario1 !== userId && chat.id_usuario2 !== userId) {
      return res.status(403).json({ ok: false, error: "No perteneces a este chat" });
    }

    
    const { data: mensajes, error } = await supabase
      .from("mensaje")
      .select("id, id_chat, id_remitente, contenido, fecha_envio")
      .eq("id_chat", chatId)
      .order("fecha_envio", { ascending: true });

    if (error) throw error;

    res.json({ ok: true, mensajes });
  } catch (err) {
    console.error("Error en GET /api/chat/:chatId/mensajes:", err);
    res.status(500).json({ ok: false, error: "Error al obtener mensajes" });
  }
});

/**
 * Crear/obtener chat vía HTTP entre dos usuarios
 * Útil si quieres abrirlo sin socket al comienzo
 */
router.post("/with-user/:otherUserId", requireAuth, async (req, res) => {
  const userId = req.user.id_usuario;
  const otherUserId = Number(req.params.otherUserId);

  if (Number.isNaN(otherUserId)) {
    return res.status(400).json({ ok: false, error: "otherUserId inválido" });
  }

  const [u1, u2] = normalizePair(userId, otherUserId);

  try {
    const { data: existing, error: selectError } = await supabase
      .from("chat")
      .select("*")
      .eq("id_usuario1", u1)
      .eq("id_usuario2", u2)
      .maybeSingle();

    if (selectError) throw selectError;

    if (existing) {
      return res.json({ ok: true, chat: existing });
    }

    const { data, error: insertError } = await supabase
      .from("chat")
      .insert([{ id_usuario1: u1, id_usuario2: u2 }])
      .select("*")
      .single();

    if (insertError) throw insertError;

    res.json({ ok: true, chat: data });
  } catch (err) {
    console.error("Error en POST /api/chat/with-user:", err);
    res.status(500).json({ ok: false, error: "No se pudo crear/obtener chat" });
  }
});

export default router;
