// services/userService.js
import path from "node:path";
import { supabase } from "../lib/supabaseClient.js";
import {
  getAllUsers,
  getUserById,
  updateUserReputation,
  updateUserPhoto,
} from "../repositories/userRepository.js";

const BUCKET = "usuarios";  // nombre del bucket de Storage

// 📜 Listar todos los usuarios
export async function listUsersService() {
  return await getAllUsers();
}

// 🔍 Obtener usuario por ID
export async function getUserByIdService(id) {
  return await getUserById(id);
}

// ⭐ Actualizar reputación
export async function updateReputationService(id, reputacion) {
  return await updateUserReputation(id, reputacion);
}

// 🖼️ Subir foto a Storage y actualizar en BD
export async function updatePhotoService(id, file) {
  if (!file) {
    throw new Error("No se recibió archivo");
  }

  const ext = path.extname(file.originalname) || ".jpg";
  const filePath = `usuario_${id}/${Date.now()}${ext}`;

  // 1) subir a Supabase Storage
  const { data, error } = await supabase.storage
    .from(BUCKET)
    .upload(filePath, file.buffer, {
      contentType: file.mimetype,
      upsert: true,
    });

  if (error) {
    console.error("Error subiendo a storage:", error);
    throw error;
  }

  // 2) obtener URL pública
  const { data: publicData } = supabase
    .storage
    .from(BUCKET)
    .getPublicUrl(filePath);

  const publicUrl = publicData.publicUrl;

  // 3) guardar URL en la tabla usuario
  const usuario = await updateUserPhoto(id, publicUrl);

  return usuario;
}
