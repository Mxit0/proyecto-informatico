import { supabase } from '../lib/supabaseClient.js';
import { getCartByUser, clearCart } from './carroRepository.js';

export async function createOrderFromCart(userId) {
  try {
    // 1. Obtener los items actuales del carrito
    const cartData = await getCartByUser(userId);
    
    if (!cartData || cartData.items.length === 0) {
      throw new Error("El carrito está vacío");
    }

    // 2. Crear la cabecera de la compra en tabla 'compras'
    const { data: newOrder, error: orderError } = await supabase
      .from('compras')
      .insert({
        id_usuario: userId,
        total: cartData.total,
        estado: 'pagado',
        fecha_compra: new Date().toISOString()
      })
      .select()
      .single();

    if (orderError) throw orderError;

    // 3. Mover los items a 'detalle_compra'
    const orderItems = cartData.items.map(item => ({
      id_compra: newOrder.id,
      id_producto: item.id_producto,
      cantidad: item.cantidad,
      precio_unitario: item.precio_unitario
    }));

    const { error: detailsError } = await supabase
      .from('detalle_compra')
      .insert(orderItems);

    if (detailsError) throw detailsError;

    // 4. Vaciar el carrito (porque ya se compró)
    await clearCart(userId);

    // 5. Devolver el ID de la orden creada
    return newOrder.id;

  } catch (err) {
    throw new Error('Error creando orden: ' + err.message);
  }
}

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