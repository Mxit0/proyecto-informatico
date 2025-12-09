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

  // 1. Insertar el foro
  const { data: nuevoForo, error } = await supabase
    .from("foro")
    .insert([{ titulo, descripcion, id_creador: userId }])
    .select()
    .single();

  if (error) {
    console.error("Error creando foro:", error);
    return res.status(500).json({ ok: false, error: "No se pudo crear el foro" });
  }

  // 2. (Opcional pero recomendado) Volver a consultar para traer los datos del usuario (foto/nombre)
  // Así la UI lo muestra bonito de inmediato sin recargar.
  const { data: foroCompleto } = await supabase
    .from("foro")
    .select("*, usuario:id_creador (nombre_usuario, foto)")
    .eq("id", nuevoForo.id)
    .single();

  res.json({ ok: true, foro: foroCompleto || nuevoForo });
});

/**
 * 🟦 Listar foros
 * MODIFICADO: Ahora trae foto y nombre del creador
 */
router.get("/", async (_req, res) => {
  const { data, error } = await supabase
    .from("foro")
    // 👇 AQUÍ ESTÁ LA MAGIA: Traemos los datos de la tabla usuario relacionada
    .select("*, usuario:id_creador (nombre_usuario, foto)") 
    .order("fecha_creacion", { ascending: false });

  if (error) {
    console.error("Error listando foros:", error);
    return res.status(500).json({ ok: false, error: "No se pudieron obtener los foros" });
  }

  res.json({ ok: true, foros: data });
});

/**
 * 🟦 Obtener detalle de un foro
 * MODIFICADO: Ahora trae foto y nombre del creador
 */
router.get("/:foroId", async (req, res) => {
  const { foroId } = req.params;

  const { data, error } = await supabase
    .from("foro")
    .select("*, usuario:id_creador (nombre_usuario, foto)") // <-- Join con usuario
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
 * ✏️ [NUEVO] Editar Foro (Solo el dueño)
 */
router.put("/:id", requireAuth, async (req, res) => {
  const { id } = req.params;
  const { titulo, descripcion } = req.body;
  const userId = req.user.id_usuario;

  // 1. Verificar dueño
  const { data: foro } = await supabase.from("foro").select("id_creador").eq("id", id).maybeSingle();
  if (!foro) return res.status(404).json({ ok: false, error: "Foro no encontrado" });
  
  if (foro.id_creador !== userId) {
    return res.status(403).json({ ok: false, error: "No tienes permiso para editar este foro" });
  }

  // 2. Actualizar
  const { data, error } = await supabase
    .from("foro")
    .update({ titulo, descripcion })
    .eq("id", id)
    .select("*, usuario:id_creador (nombre_usuario, foto)") // Devolver actualizado con usuario
    .single();

  if (error) return res.status(500).json({ ok: false, error: error.message });

  res.json({ ok: true, foro: data });
});

/**
 * 🗑️ [NUEVO] Eliminar Foro (Solo el dueño)
 */
router.delete("/:id", requireAuth, async (req, res) => {
  const { id } = req.params;
  const userId = req.user.id_usuario;

  // 1. Verificar dueño
  const { data: foro } = await supabase.from("foro").select("id_creador").eq("id", id).maybeSingle();
  if (!foro) return res.status(404).json({ ok: false, error: "Foro no encontrado" });

  if (foro.id_creador !== userId) {
    return res.status(403).json({ ok: false, error: "No tienes permiso para eliminar este foro" });
  }

  // 2. Eliminar Publicaciones asociadas primero (Limpieza manual si no hay cascade en BD)
  await supabase.from("publicacion").delete().eq("id_foro", id);

  // 3. Eliminar Foro
  const { error } = await supabase.from("foro").delete().eq("id", id);

  if (error) return res.status(500).json({ ok: false, error: error.message });

  res.json({ ok: true });
});

/**
 * 🟩 Listar publicaciones de un foro
 * MODIFICADO: Ahora trae foto y nombre del usuario que comentó
 */
router.get("/:foroId/publicaciones", async (req, res) => {
  const { foroId } = req.params;

  const { data, error } = await supabase
    .from("publicacion")
    // 👇 AQUÍ ESTÁ LA MAGIA: Traemos los datos del usuario que comenta
    .select("*, usuario:id_usuario (nombre_usuario, foto)")
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
 */
router.post("/:foroId/publicaciones", requireAuth, async (req, res) => {
  const { foroId } = req.params;
  const { contenido, id_respuesta_a } = req.body;
  const userId = req.user.id_usuario;

  if (!contenido || !contenido.trim()) {
    return res.status(400).json({ ok: false, error: "El contenido es obligatorio" });
  }

  // 1. Insertar
  const { data: nuevaPublicacion, error } = await supabase
    .from("publicacion")
    .insert([
      {
        id_foro: foroId,
        id_usuario: userId,
        contenido,
        id_respuesta_a: id_respuesta_a || null,
      },
    ])
    .select()
    .single();

  if (error) {
    console.error("Error creando publicación:", error);
    return res.status(500).json({ ok: false, error: "No se pudo crear la publicación" });
  }

  // 2. Recuperar la publicación CON los datos del usuario (JOIN)
  // Esto es vital para que el Socket envíe la foto y nombre a todos los conectados
  const { data: publicacionCompleta } = await supabase
    .from("publicacion")
    .select("*, usuario:id_usuario (nombre_usuario, foto)")
    .eq("id", nuevaPublicacion.id)
    .single();

  // EMITIR SOCKET.IO AL FORO
  try {
    const io = req.app.get("io");
    if (io) {
      // Enviamos la versión completa con foto y nombre
      io.to(`foro_${foroId}`).emit("new_forum_post", publicacionCompleta);
    }
  } catch (e) {
    console.error("No se pudo emitir new_forum_post:", e.message);
  }

  res.json({ ok: true, publicacion: publicacionCompleta });
});

/**
 * ✏️ Editar una publicación
 */
router.put("/publicaciones/:publicacionId", requireAuth, async (req, res) => {
  const { publicacionId } = req.params;
  const { contenido } = req.body;
  const userId = req.user.id_usuario;

  if (!contenido || !contenido.trim()) {
    return res.status(400).json({ ok: false, error: "Contenido requerido" });
  }

  // Verificar dueño
  const { data: pub, error: pubError } = await supabase
    .from("publicacion")
    .select("id, id_usuario")
    .eq("id", publicacionId)
    .maybeSingle();

  if (pubError || !pub) return res.status(404).json({ ok: false, error: "Publicación no encontrada" });

  if (pub.id_usuario !== userId) {
    return res.status(403).json({ ok: false, error: "No puedes editar esta publicación" });
  }

  // Actualizar
  const { data, error } = await supabase
    .from("publicacion")
    .update({ contenido })
    .eq("id", publicacionId)
    .select("*, usuario:id_usuario (nombre_usuario, foto)") // Devolver con datos de usuario
    .single();

  if (error) return res.status(500).json({ ok: false, error: error.message });

  res.json({ ok: true, publicacion: data });
});

/**
 * 🗑️ Eliminar una publicación
 */
router.delete("/publicaciones/:publicacionId", requireAuth, async (req, res) => {
  const { publicacionId } = req.params;
  const userId = req.user.id_usuario;

  // Verificar dueño
  const { data: pub, error: pubError } = await supabase
    .from("publicacion")
    .select("id, id_usuario")
    .eq("id", publicacionId)
    .maybeSingle();

  if (pubError || !pub) return res.status(404).json({ ok: false, error: "Publicación no encontrada" });

  if (pub.id_usuario !== userId) {
    return res.status(403).json({ ok: false, error: "No puedes eliminar esta publicación" });
  }

  const { error } = await supabase
    .from("publicacion")
    .delete()
    .eq("id", publicacionId);

  if (error) return res.status(500).json({ ok: false, error: "No se pudo eliminar" });

  res.json({ ok: true });
});

// ... El resto (Likes) queda igual ...
router.post("/publicaciones/:publicacionId/like", requireAuth, async (req, res) => {
    // ... (Tu código de likes existente está perfecto, no necesita cambios)
    const { publicacionId } = req.params;
    const { tipo } = req.body; // 1 o -1
    const userId = req.user.id_usuario;

    if (![1, -1].includes(tipo)) {
        return res.status(400).json({ ok: false, error: "Tipo inválido (usar 1 o -1)" });
    }

    const { data: existing, error: existingError } = await supabase
        .from("publicacion_like")
        .select("*")
        .eq("id_publicacion", publicacionId)
        .eq("id_usuario", userId)
        .maybeSingle();

    if (existingError) return res.status(500).json({ ok: false, error: "Error al revisar like" });

    if (existing && existing.tipo === tipo) {
        await supabase.from("publicacion_like").delete().eq("id", existing.id);
        return res.json({ ok: true, like: null });
    }

    if (existing && existing.tipo !== tipo) {
        const { data } = await supabase.from("publicacion_like").update({ tipo }).eq("id", existing.id).select().single();
        return res.json({ ok: true, like: data });
    }

    const { data, error } = await supabase.from("publicacion_like").insert([{ id_publicacion: publicacionId, id_usuario: userId, tipo }]).select().single();
    if (error) return res.status(500).json({ ok: false, error: "No se pudo crear like" });

    res.json({ ok: true, like: data });
});

export default router;