import { supabase } from '../lib/supabaseClient.js';

/**
 * Obtiene o crea un carrito para el usuario.
 * Retorna el ID del carrito.
 */
async function getOrCreateCartId(userId) {
  // 1. Intentamos buscar el carrito existente
  const { data: cart, error } = await supabase
    .from('carrito')
    .select('id')
    .eq('id_usuario', userId)
    .maybeSingle();

  if (cart) return cart.id;

  // 2. Si no existe, lo creamos
  // Nota: Supabase pondrá fecha_creacion automáticamente si tiene default, 
  // si no, podrías necesitar agregar: fecha_creacion: new Date()
  const { data: newCart, error: createError } = await supabase
    .from('carrito')
    .insert({ id_usuario: userId })
    .select('id')
    .single();

  if (createError) throw createError;
  return newCart.id;
}

/**
 * Agrega un producto al carrito o actualiza la cantidad si ya existe.
 */
export async function addToCart(userId, productId, quantity) {
  try {
    const cartId = await getOrCreateCartId(userId);

    // Verificamos si el item ya está en la lista
    const { data: existingItem, error: fetchError } = await supabase
      .from('listacarrito') // <--- Tabla corregida
      .select('id, cantidad')
      .eq('id_carrito', cartId)
      .eq('id_producto', productId)
      .maybeSingle();

    if (fetchError) throw fetchError;

    let data;
    let error;

    if (existingItem) {
      // UPDATE: Sumamos cantidad
      const newQuantity = existingItem.cantidad + quantity;
      ({ data, error } = await supabase
        .from('listacarrito')
        .update({ cantidad: newQuantity })
        .eq('id', existingItem.id)
        .select());
    } else {
      // INSERT: Creamos el item
      // Ojo: No estamos llenando 'precio_seleccionado' aquí, 
      // porque usualmente en el carrito se muestra el precio actual de la tienda.
      ({ data, error } = await supabase
        .from('listacarrito')
        .insert({
          id_carrito: cartId,
          id_producto: productId,
          cantidad: quantity
        })
        .select());
    }

    if (error) throw error;
    return data;

  } catch (err) {
    throw new Error('Error en addToCart: ' + err.message);
  }
}

/**
 * Obtiene el carrito completo con detalles del producto (Nombre, Precio, Imagen).
 */
export async function getCartByUser(userId) {
  try {
    // 1. Obtener ID del carrito
    const { data: cart } = await supabase
      .from('carrito')
      .select('id')
      .eq('id_usuario', userId)
      .maybeSingle();

    if (!cart) return { items: [], total: 0 };

    // 2. Obtener items de listacarrito
    const { data: items, error: itemsError } = await supabase
      .from('listacarrito') // <--- Tabla corregida
      .select('id, id_producto, cantidad')
      .eq('id_carrito', cart.id);

    if (itemsError) throw itemsError;
    if (items.length === 0) return { items: [], total: 0 };

    // 3. Obtener detalles de los PRODUCTOS (Nombre, Precio, Stock) desde tabla 'producto'
    const productIds = items.map(i => i.id_producto);
    

    const { data: productsData, error: productsError } = await supabase
      .from('producto') 
      .select('id, nombre, precio, stock')
      .in('id', productIds);

    if (productsError) throw productsError;

    // 4. Obtener UNA imagen principal por producto
    const { data: imagesData, error: imagesError } = await supabase
      .from('producto_imagenes')
      .select('id_prod, url_imagen')
      .in('id_prod', productIds);
      
    if (imagesError) throw imagesError;

    // 5. UNIFICAR TODO
    let totalCarrito = 0;

    const fullItems = items.map(item => {
      const productInfo = productsData.find(p => p.id === item.id_producto);
      // Tomamos la primera imagen que encontremos para este producto
      const productImg = imagesData.find(img => img.id_prod === item.id_producto);
      
      const precioUnitario = productInfo ? productInfo.precio : 0;
      const subtotal = precioUnitario * item.cantidad;
      totalCarrito += subtotal;

      return {
        id_item_lista: item.id, // ID de la fila en listacarrito
        id_producto: item.id_producto,
        cantidad: item.cantidad,
        nombre: productInfo ? productInfo.nombre : 'Producto no disponible',
        precio_unitario: precioUnitario,
        stock_disponible: productInfo ? productInfo.stock : 0,
        imagen: productImg ? productImg.url_imagen : null,
        subtotal: subtotal
      };
    });

    return { 
      cart_id: cart.id,
      items: fullItems, 
      total: totalCarrito 
    };

  } catch (err) {
    throw new Error('Error en getCartByUser: ' + err.message);
  }
}

/**
 * Elimina un item específico de listacarrito.
 */
export async function removeItemFromCart(userId, productId) {
  try {
    const cartId = await getOrCreateCartId(userId);
    
    const { error } = await supabase
      .from('listacarrito') // <--- Tabla corregida
      .delete()
      .eq('id_carrito', cartId)
      .eq('id_producto', productId);

    if (error) throw error;
    return true;
  } catch (err) {
    throw new Error('Error eliminando item: ' + err.message);
  }
}

/**
 * Vaciar carrito completo.
 */
export async function clearCart(userId) {
   try {
    const cartId = await getOrCreateCartId(userId);
    
    const { error } = await supabase
      .from('listacarrito') // <--- Tabla corregida
      .delete()
      .eq('id_carrito', cartId);

    if (error) throw error;
    return true;
  } catch (err) {
    throw new Error('Error vaciando carrito: ' + err.message);
  }
}