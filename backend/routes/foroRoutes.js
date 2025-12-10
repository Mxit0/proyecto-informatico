// backend/routes/foroRoutes.js
import express from "express";
import { supabase } from "../lib/supabaseClient.js";
import { requireAuth } from "../middleware/auth.js";

const router = express.Router();

/**
 * 🛠️ FUNCIÓN AUXILIAR: Enriquecer con datos de usuario
 * Busca manualmente los nombres y fotos de los usuarios para evitar errores de Foreign Key.
 */
async function enrichWithUser(items, userIdField, userAlias = 'usuario') {
  if (!items || items.length === 0) return [];
  
  // 1. Obtener todos los IDs de usuario únicos de la lista
  const itemsArray = Array.isArray(items) ? items : [items];
  const userIds = [...new Set(itemsArray.map(item => item[userIdField]))];
  
  // 2. Buscar info de esos usuarios
  const { data: users } = await supabase
      .from("usuario")
      .select("id_usuario, nombre_usuario, foto")
      .in("id_usuario", userIds);
      
  // 3. Pegar la info del usuario a cada item
  const enriched = itemsArray.map(item => {
      const user = users?.find(u => u.id_usuario === item[userIdField]);
      return {
          ...item,
          [userAlias]: user ? {
              nombre_usuario: user.nombre_usuario,
              foto: user.foto
          } : null
      };
  });

  return Array.isArray(items) ? enriched : enriched[0];
}

/**
 * Crear un nuevo foro (tema)
 */
router.post("/", requireAuth, async (req, res) => {
  const { titulo, descripcion } = req.body;
  const userId = req.user.id_usuario;

  if (!titulo) return res.status(400).json({ ok: false, error: "Título requerido" });

  const { data: nuevoForo, error } = await supabase
    .from("foro")
    .insert([{ titulo, descripcion, id_creador: userId }])
    .select()
    .single();

  if (error) return res.status(500).json({ ok: false, error: error.message });

  // Enriquecer manualmente
  const foroCompleto = await enrichWithUser(nuevoForo, "id_creador");
  res.json({ ok: true, foro: foroCompleto });
});

/**
 * Listar foros
 */
router.get("/", async (_req, res) => {
  const { data, error } = await supabase
    .from("foro")
    .select("*") // Seleccionamos datos crudos
    .order("fecha_creacion", { ascending: false });

  if (error) return res.status(500).json({ ok: false, error: error.message });

  // Enriquecemos la lista manualmente
  const forosEnriquecidos = await enrichWithUser(data, "id_creador");
  res.json({ ok: true, foros: forosEnriquecidos });
});

/**
 * Obtener detalle de un foro
 */
router.get("/:foroId", async (req, res) => {
  const { foroId } = req.params;

  const { data, error } = await supabase
    .from("foro")
    .select("*")
    .eq("id", foroId)
    .maybeSingle();

  if (error || !data) return res.status(404).json({ ok: false, error: "Foro no encontrado" });

  const foroEnriquecido = await enrichWithUser(data, "id_creador");
  res.json({ ok: true, foro: foroEnriquecido });
});

/**
 * Editar Foro
 */
router.put("/:id", requireAuth, async (req, res) => {
  const { id } = req.params;
  const { titulo, descripcion } = req.body;
  const userId = req.user.id_usuario;

  // Verificar dueño
  const { data: foro } = await supabase.from("foro").select("id_creador").eq("id", id).maybeSingle();
  if (!foro || foro.id_creador !== userId) {
    return res.status(403).json({ ok: false, error: "Sin permiso" });
  }

  const { data: actualizado, error } = await supabase
    .from("foro")
    .update({ titulo, descripcion })
    .eq("id", id)
    .select()
    .single();

  if (error) return res.status(500).json({ ok: false, error: error.message });

  const final = await enrichWithUser(actualizado, "id_creador");
  res.json({ ok: true, foro: final });
});

/**
 * Eliminar Foro
 */
router.delete("/:id", requireAuth, async (req, res) => {
  const { id } = req.params;
  const userId = req.user.id_usuario;

  const { data: foro } = await supabase.from("foro").select("id_creador").eq("id", id).maybeSingle();
  if (!foro || foro.id_creador !== userId) {
    return res.status(403).json({ ok: false, error: "Sin permiso" });
  }

  await supabase.from("publicacion").delete().eq("id_foro", id);
  const { error } = await supabase.from("foro").delete().eq("id", id);
  
  if (error) return res.status(500).json({ ok: false, error: error.message });
  res.json({ ok: true });
});

/**
 * Listar publicaciones de un foro
 */
router.get("/:foroId/publicaciones", async (req, res) => {
  const { foroId } = req.params;

  const { data, error } = await supabase
    .from("publicacion")
    .select("*")
    .eq("id_foro", foroId)
    .order("fecha_publicacion", { ascending: true });

  if (error) return res.status(500).json({ ok: false, error: error.message });

  // Enriquecemos con los datos del usuario que comentó (usando 'id_usuario')
  const publicacionesFull = await enrichWithUser(data, "id_usuario");
  res.json({ ok: true, publicaciones: publicacionesFull });
});

/**
 * Crear publicación (Post)
 */
router.post("/:foroId/publicaciones", requireAuth, async (req, res) => {
  const { foroId } = req.params;
  const { contenido, id_respuesta_a } = req.body;
  const userId = req.user.id_usuario;

  if (!contenido) return res.status(400).json({ ok: false, error: "Contenido vacío" });

  const { data: nueva, error } = await supabase
    .from("publicacion")
    .insert([{ id_foro: foroId, id_usuario: userId, contenido, id_respuesta_a: id_respuesta_a || null }])
    .select()
    .single();

  if (error) return res.status(500).json({ ok: false, error: error.message });

  // Enriquecemos MANUALMENTE para que el Socket envíe la foto y nombre
  const publicacionCompleta = await enrichWithUser(nueva, "id_usuario");

  // EMITIR SOCKET
  try {
    const io = req.app.get("io");
    if (io) {
      io.to(`foro_${foroId}`).emit("new_forum_post", publicacionCompleta);
    }
  } catch (e) {
    console.error("Socket error:", e);
  }

  res.json({ ok: true, publicacion: publicacionCompleta });
});

/**
 * Editar publicación
 */
router.put("/publicaciones/:publicacionId", requireAuth, async (req, res) => {
  const { publicacionId } = req.params;
  const { contenido } = req.body;
  const userId = req.user.id_usuario;

  const { data: pub } = await supabase.from("publicacion").select("id_usuario").eq("id", publicacionId).maybeSingle();
  if (!pub || pub.id_usuario !== userId) return res.status(403).json({ ok: false, error: "Sin permiso" });

  const { data: updated, error } = await supabase
    .from("publicacion")
    .update({ contenido })
    .eq("id", publicacionId)
    .select()
    .single();

  if (error) return res.status(500).json({ ok: false, error: error.message });

  const final = await enrichWithUser(updated, "id_usuario");
  res.json({ ok: true, publicacion: final });
});

/**
 * Eliminar publicación
 */
router.delete("/publicaciones/:publicacionId", requireAuth, async (req, res) => {
  const { publicacionId } = req.params;
  const userId = req.user.id_usuario;

  const { data: pub } = await supabase.from("publicacion").select("id_usuario").eq("id", publicacionId).maybeSingle();
  if (!pub || pub.id_usuario !== userId) return res.status(403).json({ ok: false, error: "Sin permiso" });

  await supabase.from("publicacion").delete().eq("id", publicacionId);
  res.json({ ok: true });
});

// Likes (se mantiene igual, no usa datos de usuario complejos en la respuesta)
router.post("/publicaciones/:publicacionId/like", requireAuth, async (req, res) => {
    const { publicacionId } = req.params;
    const { tipo } = req.body;
    const userId = req.user.id_usuario;

    if (![1, -1].includes(tipo)) return res.status(400).json({ ok: false, error: "Tipo inválido" });

    const { data: existing } = await supabase
        .from("publicacion_like")
        .select("*")
        .eq("id_publicacion", publicacionId)
        .eq("id_usuario", userId)
        .maybeSingle();

    if (existing && existing.tipo === tipo) {
        await supabase.from("publicacion_like").delete().eq("id", existing.id);
        return res.json({ ok: true, like: null });
    }

    if (existing) {
        const { data } = await supabase.from("publicacion_like").update({ tipo }).eq("id", existing.id).select().single();
        return res.json({ ok: true, like: data });
    }

    const { data, error } = await supabase.from("publicacion_like").insert([{ id_publicacion: publicacionId, id_usuario: userId, tipo }]).select().single();
    if (error) return res.status(500).json({ ok: false, error: error.message });

    res.json({ ok: true, like: data });
});

export default router;