// repositories/userRepository.js
import { supabase } from '../lib/supabaseClient.js';

const TABLE = 'usuario';

// ⚠️ Ya lo tienes:
export async function findUserByEmail(correo) {
  const { data, error } = await supabase
    .from(TABLE)
    .select('id_usuario, nombre_usuario, correo, password, foto, reputacion')
    .eq('correo', correo)
    .maybeSingle();

  if (error) throw error;
  return data;
}

export async function createUser({ nombre_usuario, correo, passwordHash, foto = null }) {
  const { data, error } = await supabase
    .from(TABLE)
    .insert([{ nombre_usuario, correo, password: passwordHash, foto }])
    .select('id_usuario, nombre_usuario, correo, foto, reputacion')
    .single();

  if (error) throw error;
  return data;
}

// 🆕 NUEVO:
export async function getAllUsers() {
  const { data, error } = await supabase
    .from(TABLE)
    .select('id_usuario, nombre_usuario, correo, foto, reputacion, fecha_registro');

  if (error) throw error;
  return data;
}

export async function getUserById(id_usuario) {
  const { data, error } = await supabase
    .from(TABLE)
    .select('id_usuario, nombre_usuario, correo, foto, reputacion, fecha_registro')
    .eq('id_usuario', id_usuario)
    .maybeSingle();

  if (error) throw error;
  return data;
}

export async function updateUserReputation(id_usuario, reputacion) {
  const { data, error } = await supabase
    .from(TABLE)
    .update({ reputacion })
    .eq('id_usuario', id_usuario)
    .select('id_usuario, nombre_usuario, correo, foto, reputacion, fecha_registro')
    .single();

  if (error) throw error;
  return data;
}
export async function updateUserPhoto(id_usuario, fotoUrl) {
  const { data, error } = await supabase
    .from(TABLE)
    .update({ foto: fotoUrl })
    .eq("id_usuario", id_usuario)
    .select("id_usuario, nombre_usuario, correo, foto, reputacion, fecha_registro")
    .single();

  if (error) throw error;
  return data;
}