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

    // --- 1. NUEVO: Verificar stock en la base de datos ---
    const { data: productData, error: productError } = await supabase
      .from('producto')
      .select('stock, nombre')
      .eq('id', productId)
      .single();

    if (productError) throw new Error("Error verificando producto");
    
    if (productData.stock < quantity) {
        throw new Error(`No hay suficiente stock de ${productData.nombre}. Disponible: ${productData.stock}`);
    }
    // ----------------------------------------------------

    // Verificamos si el item ya está en la lista (Lógica existente)
    const { data: existingItem, error: fetchError } = await supabase
      .from('listacarrito')
      .select('id, cantidad')
      .eq('id_carrito', cartId)
      .eq('id_producto', productId)
      .maybeSingle();

    if (fetchError) throw fetchError;

    let data;
    let error;

    if (existingItem) {
      // UPDATE: Sumamos cantidad
      // Opcional: Validar también que (existingItem.cantidad + quantity) no supere el stock
      if ((existingItem.cantidad + quantity) > productData.stock) {
         throw new Error(`No puedes añadir más. Stock máximo alcanzado.`);
      }

      const newQuantity = existingItem.cantidad + quantity;
      ({ data, error } = await supabase
        .from('listacarrito')
        .update({ cantidad: newQuantity })
        .eq('id', existingItem.id)
        .select());
    } else {
      // INSERT
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
    throw new Error(err.message);
  }
}

/**
 * Obtiene el carrito completo con detalles del producto (Nombre, Precio, Imagen).
 */
export async function getCartByUser(userId) {
  try {
   
    const { data: cart } = await supabase
      .from('carrito')
      .select('id')
      .eq('id_usuario', userId)
      .maybeSingle();

    if (!cart) return { items: [], total: 0 };

    
    const { data: items, error: itemsError } = await supabase
      .from('listacarrito') 
      .select('id, id_producto, cantidad')
      .eq('id_carrito', cart.id);

    if (itemsError) throw itemsError;
    if (items.length === 0) return { items: [], total: 0 };

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

    
    let totalCarrito = 0;

    const fullItems = items.map(item => {
      const productInfo = productsData.find(p => p.id === item.id_producto);
      
      const productImg = imagesData.find(img => img.id_prod === item.id_producto);
      
      const precioUnitario = productInfo ? productInfo.precio : 0;
      const subtotal = precioUnitario * item.cantidad;
      totalCarrito += subtotal;

      return {
        id_item_lista: item.id, 
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
      .from('listacarrito') 
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
      .from('listacarrito') 
      .delete()
      .eq('id_carrito', cartId);

    if (error) throw error;
    return true;
  } catch (err) {
    throw new Error('Error vaciando carrito: ' + err.message);
  }
}