import { supabase } from "../lib/supabaseClient.js";

const TABLE_COMPONENTE = "componente_maestro";

/**
 * Obtiene los componentes maestros asociados a una categoría
 */
export async function getComponentsByCategory(categoryId) {
  try {
    const { data, error } = await supabase
      .from(TABLE_COMPONENTE)
      .select("id, nombre_componente, categoria, especificaciones")
      .eq("categoria", categoryId)
      .order("nombre_componente", { ascending: true });

    if (error) throw error;
    return data || [];
  } catch (err) {
    console.error("Error en getComponentsByCategory:", err);
    throw err;
  }
}

export async function getComponentById(id) {
  try {
    const { data, error } = await supabase
      .from(TABLE_COMPONENTE)
      .select("id, nombre_componente, categoria, especificaciones")
      .eq("id", id)
      .single();

    if (error) {
      // Si la fila no existe, Supabase devuelve error; retornamos null para facilitar validación
      if (error.code === "PGRST116") return null;
      throw error;
    }

    return data || null;
  } catch (err) {
    console.error("Error en getComponentById:", err);
    throw err;
  }
}
