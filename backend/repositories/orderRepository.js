import { supabase } from '../lib/supabaseClient.js';


//FUNCIONES DEDICADAS PARA EL HISTORIAL DE COMPRAS
// OBTENER LA LISTA DE COMPRAS DE UN USUARIO
export async function getOrdersByUser(userId) {
  try {
    const { data, error } = await supabase
      .from('compras')
      .select('*')
      .eq('id_usuario', userId)
      .order('fecha_compra', { ascending: false });

    if (error) throw error;
    return data;
  } catch (err) {
    throw new Error('Error obteniendo historial: ' + err.message);
  }
}

// OBTENER DETALLE DE UNA COMPRA ESPECÍFICA
//PARA CUANDO SE PULSE UNA ORDEN EN EL HISTORIAL
export async function getOrderById(orderId) {
  try {
    
    const { data: compra, error: compraError } = await supabase
      .from('compras')
      .select('*')
      .eq('id', orderId)
      .single();

    if (compraError) throw compraError;
    if (!compra) return null;

    
    const { data: detalles, error: detallesError } = await supabase
      .from('detalle_compra')
      .select('*')
      .eq('id_compra', orderId);

    if (detallesError) throw detallesError;

   
    const productIds = detalles.map(d => d.id_producto);

   
    const { data: productsInfo } = await supabase
      .from('producto')
      .select('id, nombre')
      .in('id', productIds);

    
    const { data: imagesInfo } = await supabase
      .from('producto_imagenes')
      .select('id_prod, url_imagen')
      .in('id_prod', productIds);

    
    const detallesCompletos = detalles.map(item => {
      const info = productsInfo.find(p => p.id === item.id_producto);
      const img = imagesInfo.find(i => i.id_prod === item.id_producto);

      return {
        ...item, 
        nombre_producto: info ? info.nombre : 'Producto eliminado',
        imagen_producto: img ? img.url_imagen : null
      };
    });

    return {
      compra: compra,
      items: detallesCompletos
    };

  } catch (err) {
    throw new Error('Error obteniendo detalle de compra: ' + err.message);
  }
}