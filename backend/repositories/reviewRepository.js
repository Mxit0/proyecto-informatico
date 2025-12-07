import { supabase } from '../lib/supabaseClient.js';

// --- FUNCIÓN AUXILIAR (Privada) ---
// Sirve para: Buscar los datos "extra" (nombres, fotos) y dar formato para que el Frontend (Kotlin) no falle.
async function enrichReviews(reviews) {
  if (!reviews || reviews.length === 0) return [];

 
  const userIds = [...new Set(reviews.map(r => r.id_usuario))];
  const productIds = [...new Set(reviews.map(r => r.id_producto))];

  
  const { data: usersData, error: usersError } = await supabase
    .from('usuario') // Tabla 'usuario'
    .select('id_usuario, nombre_usuario, foto') 
    .in('id_usuario', userIds);
  
  if (usersError) throw usersError;


  const { data: productsData, error: productsError } = await supabase
    .from('producto') // Tabla 'producto'
    .select('id, nombre, id_usuario') 
    .in('id', productIds);
  
  if (productsError) throw productsError;

  
  const { data: imagesData, error: imagesError } = await supabase
    .from('producto_imagenes')
    .select('id_prod, url_imagen')
    .in('id_prod', productIds);
    
  if (imagesError) throw imagesError;

  
  return reviews.map(r => {
    
    const user = usersData.find(u => u.id_usuario === r.id_usuario);
    const product = productsData.find(p => p.id === r.id_producto);
    const prodImg = imagesData.find(img => img.id_prod === r.id_producto);

    return {
      id: String(r.id), 
      
      
      productId: String(r.id_producto),
      productName: product ? product.nombre : 'Producto Desconocido',
      productImageUrl: prodImg ? prodImg.url_imagen : '', 
      
      
      author: user ? user.nombre_usuario : `Usuario #${r.id_usuario}`, 
      authorId: String(r.id_usuario),
      authorImageUrl: user ? user.foto : null, 
      
      
      date: r.fecha, 
      rating: Number(r.calificacion), 
      comment: r.comentario || '',
      
      
      likedByUserIds: [] 
    };
  });
}

// --- FUNCIONES EXPORTADAS (Las que usa el Router) ---


export async function addReview({ id_producto, id_usuario, calificacion, comentario }) {
  try {
    const { data, error } = await supabase
      .from('resenas')
      .insert([{ id_producto, id_usuario, calificacion, comentario }])
      .select()
      .single();

    if (error) {
      
      if (error.code === '23505') throw new Error('Ya has calificado este producto anteriormente.');
      throw error;
    }
    return data;
  } catch (err) {
    throw new Error('Error al crear reseña: ' + err.message);
  }
}


export async function getReviewsByProduct(productId) {
  try {
    const { data: reviews, error } = await supabase
      .from('resenas')
      .select('*')
      .eq('id_producto', productId)
      .order('fecha', { ascending: false });

    if (error) throw error;
    return await enrichReviews(reviews); 
  } catch (err) {
    throw new Error('Error obteniendo reseñas del producto: ' + err.message);
  }
}


export async function getReviewsWrittenByUser(userId) {
  try {
    const { data: reviews, error } = await supabase
      .from('resenas')
      .select('*')
      .eq('id_usuario', userId)
      .order('fecha', { ascending: false });

    if (error) throw error;
    return await enrichReviews(reviews);
  } catch (err) {
    throw new Error('Error obteniendo historial de reseñas: ' + err.message);
  }
}


export async function getReviewsReceivedBySeller(sellerId) {
  try {
    
    const { data: sellerProducts, error: prodError } = await supabase
      .from('producto')
      .select('id')
      .eq('id_usuario', sellerId); 

    if (prodError) throw prodError;
    
   
    if (!sellerProducts || sellerProducts.length === 0) return [];
    
    const productIds = sellerProducts.map(p => p.id);

    
    const { data: reviews, error: revError } = await supabase
      .from('resenas')
      .select('*')
      .in('id_producto', productIds)
      .order('fecha', { ascending: false });

    if (revError) throw revError;

    return await enrichReviews(reviews);
  } catch (err) {
    throw new Error('Error obteniendo reputación del vendedor: ' + err.message);
  }
}