// repositories/userRepository.js
import { supabase } from '../lib/supabaseClient.js';

const TABLE = 'usuario';


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

//Obtener todos los usuarios
export async function getAllUsers() {
  const { data, error } = await supabase
    .from(TABLE)
    .select('id_usuario, nombre_usuario, correo, foto, reputacion, fecha_registro');

  if (error) throw error;
  return data;
}
//Obtener usuario por ID, incluyendo reputación calculada
export async function getUserById(id_usuario) {
  
  const { data: user, error } = await supabase
    .from('usuario')
    .select('id_usuario, nombre_usuario, correo, foto, reputacion, fecha_registro')
    .eq('id_usuario', id_usuario)
    .maybeSingle();

  if (error) throw error;
  if (!user) return null;

  
  const { data: reviews, error: reviewsError } = await supabase
    .from('resenas_usuarios')
    .select('calificacion')
    .eq('id_destinatario', id_usuario);

  if (reviewsError) console.error("Error obteniendo calificaciones:", reviewsError);

  
  let realReputation = 0.0;
  let totalReviews = 0;

  if (reviews && reviews.length > 0) {
    totalReviews = reviews.length;
    
    const sum = reviews.reduce((acc, curr) => acc + Number(curr.calificacion), 0);
    
    realReputation = sum / totalReviews;
  }

  
  return {
    ...user,
    reputacion: Number(realReputation.toFixed(1)), 
    total_resenas: totalReviews
  };
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