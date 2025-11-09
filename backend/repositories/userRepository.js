import { supabase } from '../lib/supabaseClient.js';

const TABLE = 'usuario';

export async function findUserByEmail(correo) {
  const { data, error } = await supabase
    .from(TABLE)
    .select('id_usuario, nombre_usuario, correo, password, foto')
    .eq('correo', correo)
    .maybeSingle();

  if (error) throw error;
  return data;
}

export async function createUser({ nombre_usuario, correo, passwordHash, foto = null }) {
  const { data, error } = await supabase
    .from(TABLE)
    .insert([{ nombre_usuario, correo, password: passwordHash, foto }])
    .select('id_usuario, nombre_usuario, correo, foto')
    .single();

  if (error) throw error;
  return data;
}
