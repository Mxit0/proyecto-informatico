// backend/routes/foroRoutes.js
import express from "express";
import { supabase } from "../lib/supabaseClient.js";
import { requireAuth } from "../middleware/auth.js";

const router = express.Router();

/**
 * 🟦 Crear un nuevo foro (tema)
 * Body: { titulo, descripcion }
 * Auth: requerido
 */
router.post("/", requireAuth, async (req, res) => {
  const { titulo, descripcion } = req.body;
  const userId = req.user.id_usuario;

  if (!titulo) {
    return res.status(400).json({ ok: false, error: "Título es requerido" });
  }

  const { data, error } = await supabase
    .from("foro")
    .insert([{ titulo, descripcion, id_creador: userId }])
    .select("*")
    .single();

  if (error) {
    console.error("Error creando foro:", error);
    return res.status(500).json({ ok: false, error: "No se pudo crear el foro" });
  }

  res.json({ ok: true, foro: data });
});

/**
 * 🟦 Listar foros
 */
router.get("/", async (_req, res) => {
  const { data, error } = await supabase
    .from("foro")
    .select("id, titulo, descripcion, fecha_creacion, id_creador")
    .order("fecha_creacion", { ascending: false });

  if (error) {
    console.error("Error listando foros:", error);
    return res.status(500).json({ ok: false, error: "No se pudieron obtener los foros" });
  }

  res.json({ ok: true, foros: data });
});

/**
 * 🟦 Obtener detalle de un foro
 */
router.get("/:foroId", async (req, res) => {
  const { foroId } = req.params;

  const { data, error } = await supabase
    .from("foro")
    .select("id, titulo, descripcion, fecha_creacion, id_creador")
    .eq("id", foroId)
    .maybeSingle();

  if (error) {
    console.error("Error obteniendo foro:", error);
    return res.status(500).json({ ok: false, error: "Error al obtener foro" });
  }

  if (!data) {
    return res.status(404).json({ ok: false, error: "Foro no encontrado" });
  }

  res.json({ ok: true, foro: data });
});

/**
 * 🟩 Listar publicaciones de un foro
 * GET /api/foro/:foroId/publicaciones
 */
router.get("/:foroId/publicaciones", async (req, res) => {
  const { foroId } = req.params;

  const { data, error } = await supabase
    .from("publicacion")
    .select("id, id_foro, id_usuario, contenido, fecha_publicacion, id_respuesta_a")
    .eq("id_foro", foroId)
    .order("fecha_publicacion", { ascending: true });

  if (error) {
    console.error("Error listando publicaciones:", error);
    return res.status(500).json({ ok: false, error: "No se pudieron obtener las publicaciones" });
  }

  res.json({ ok: true, publicaciones: data });
});

/**
 * 🟩 Crear una publicación en un foro
 * POST /api/foro/:foroId/publicaciones
 * Body: { contenido, id_respuesta_a? }
 */
router.post("/:foroId/publicaciones", requireAuth, async (req, res) => {
  const { foroId } = req.params;
  const { contenido, id_respuesta_a } = req.body;
  const userId = req.user.id_usuario;

  if (!contenido || !contenido.trim()) {
    return res.status(400).json({ ok: false, error: "El contenido es obligatorio" });
  }

  const { data, error } = await supabase
    .from("publicacion")
    .insert([
      {
        id_foro: foroId,
        id_usuario: userId,
        contenido,
        id_respuesta_a: id_respuesta_a || null,
      },
    ])
    .select("*")
    .single();

  if (error) {
    console.error("Error creando publicación:", error);
    return res.status(500).json({ ok: false, error: "No se pudo crear la publicación" });
  }

  // EMITIR SOCKET.IO AL FORO
  try {
    const io = req.app.get("io");             // <-- recuperamos io que seteamos en server.js
    if (io) {
      io.to(`foro_${foroId}`).emit("new_forum_post", data);
    }
  } catch (e) {
    console.error("No se pudo emitir new_forum_post:", e.message);
    // No rompemos la respuesta aunque el socket falle
  }

  res.json({ ok: true, publicacion: data });
});

/**
 * ✏️ Editar una publicación
 * PUT /api/foro/publicaciones/:publicacionId
 * Body: { contenido }
 */
router.put("/publicaciones/:publicacionId", requireAuth, async (req, res) => {
  const { publicacionId } = req.params;
  const { contenido } = req.body;
  const userId = req.user.id_usuario;

  if (!contenido || !contenido.trim()) {
    return res.status(400).json({ ok: false, error: "Contenido requerido" });
  }

  // Verificar que la publicación exista y pertenezca al usuario
  const { data: pub, error: pubError } = await supabase
    .from("publicacion")
    .select("id, id_usuario")
    .eq("id", publicacionId)
    .maybeSingle();

  if (pubError) {
    console.error("Error obteniendo publicación:", pubError);
    return res.status(500).json({ ok: false, error: "Error al obtener publicación" });
  }

  if (!pub) {
    return res.status(404).json({ ok: false, error: "Publicación no encontrada" });
  }

  if (pub.id_usuario !== userId) {
    return res.status(403).json({ ok: false, error: "No puedes editar esta publicación" });
  }

  const { data, error } = await supabase
    .from("publicacion")
    .update({ contenido })
    .eq("id", publicacionId)
    .select("*")
    .single();

  if (error) {
    console.error("Error actualizando publicación:", error);
    return res.status(500).json({ ok: false, error: "No se pudo actualizar la publicación" });
  }

  res.json({ ok: true, publicacion: data });
});

/**
 * 🗑️ Eliminar una publicación
 * DELETE /api/foro/publicaciones/:publicacionId
 */
router.delete("/publicaciones/:publicacionId", requireAuth, async (req, res) => {
  const { publicacionId } = req.params;
  const userId = req.user.id_usuario;

  const { data: pub, error: pubError } = await supabase
    .from("publicacion")
    .select("id, id_usuario")
    .eq("id", publicacionId)
    .maybeSingle();

  if (pubError) {
    console.error("Error obteniendo publicación:", pubError);
    return res.status(500).json({ ok: false, error: "Error al obtener publicación" });
  }

  if (!pub) {
    return res.status(404).json({ ok: false, error: "Publicación no encontrada" });
  }

  // Podrías permitir que admin/moderador también elimine, aquí por ahora solo dueño:
  if (pub.id_usuario !== userId) {
    return res.status(403).json({ ok: false, error: "No puedes eliminar esta publicación" });
  }

  const { error } = await supabase
    .from("publicacion")
    .delete()
    .eq("id", publicacionId);

  if (error) {
    console.error("Error eliminando publicación:", error);
    return res.status(500).json({ ok: false, error: "No se pudo eliminar la publicación" });
  }

  res.json({ ok: true });
});

/**
 * 👍 / 👎 Dar like o dislike a una publicación
 * POST /api/foro/publicaciones/:publicacionId/like
 * Body: { tipo }  // 1 = like, -1 = dislike
 */
router.post("/publicaciones/:publicacionId/like", requireAuth, async (req, res) => {
  const { publicacionId } = req.params;
  const { tipo } = req.body; // 1 o -1
  const userId = req.user.id_usuario;

  if (![1, -1].includes(tipo)) {
    return res.status(400).json({ ok: false, error: "Tipo inválido (usar 1 o -1)" });
  }

  // Verificar si ya existe un like de este usuario para esta publicación
  const { data: existing, error: existingError } = await supabase
    .from("publicacion_like")
    .select("*")
    .eq("id_publicacion", publicacionId)
    .eq("id_usuario", userId)
    .maybeSingle();

  if (existingError) {
    console.error("Error revisando like:", existingError);
    return res.status(500).json({ ok: false, error: "Error al revisar like" });
  }

  // Si ya existe y el tipo es el mismo -> quitar el like (toggle off)
  if (existing && existing.tipo === tipo) {
    const { error: deleteError } = await supabase
      .from("publicacion_like")
      .delete()
      .eq("id", existing.id);

    if (deleteError) {
      console.error("Error eliminando like:", deleteError);
      return res.status(500).json({ ok: false, error: "No se pudo actualizar like" });
    }

    return res.json({ ok: true, like: null });
  }

  // Si ya existe pero con otro tipo -> actualizar
  if (existing && existing.tipo !== tipo) {
    const { data, error: updateError } = await supabase
      .from("publicacion_like")
      .update({ tipo })
      .eq("id", existing.id)
      .select("*")
      .single();

    if (updateError) {
      console.error("Error actualizando like:", updateError);
      return res.status(500).json({ ok: false, error: "No se pudo actualizar like" });
    }

    return res.json({ ok: true, like: data });
  }

  // Si no existe -> crear
  const { data, error } = await supabase
    .from("publicacion_like")
    .insert([
      {
        id_publicacion: publicacionId,
        id_usuario: userId,
        tipo,
      },
    ])
    .select("*")
    .single();

  if (error) {
    console.error("Error creando like:", error);
    return res.status(500).json({ ok: false, error: "No se pudo crear like" });
  }

  res.json({ ok: true, like: data });
});

export default router;

