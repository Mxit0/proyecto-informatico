import { supabase } from '../lib/supabaseClient.js';


// Buscar los datos "extra" (nombres, fotos) y dar formato para que el Frontend (Kotlin) no falle.
async function enrichReviews(reviews) {
  if (!reviews || reviews.length === 0) return [];

 
  const userIds = [...new Set(reviews.map(r => r.id_usuario))];
  const productIds = [...new Set(reviews.map(r => r.id_producto))];
  const reviewIds = reviews.map(r => r.id);

  
  const { data: usersData, error: usersError } = await supabase
    .from('usuario') 
    .select('id_usuario, nombre_usuario, foto') 
    .in('id_usuario', userIds);
  
  if (usersError) throw usersError;


  const { data: productsData, error: productsError } = await supabase
    .from('producto')
    .select('id, nombre, id_usuario') 
    .in('id', productIds);
  
  if (productsError) throw productsError;

  
  const { data: imagesData, error: imagesError } = await supabase
    .from('producto_imagenes')
    .select('id_prod, url_imagen')
    .in('id_prod', productIds);
    
  if (imagesError) throw imagesError;

  const { data: likesData, error: likesError } = await supabase
    .from('resena_likes')
    .select('id_resena, id_usuario')
    .in('id_resena', reviewIds);

  if (likesError) console.error("Error fetching likes", likesError);

  
  return reviews.map(r => {
    
    const user = usersData.find(u => u.id_usuario === r.id_usuario);
    const product = productsData.find(p => p.id === r.id_producto);
    const prodImg = imagesData.find(img => img.id_prod === r.id_producto);
    const reviewLikes = likesData
        ? likesData.filter(l => l.id_resena === r.id).map(l => String(l.id_usuario))
        : [];

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
      
      
      likedByUserIds: reviewLikes 
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

export async function deleteReview(reviewId, userId) {
  try {
    
    const { error } = await supabase
      .from('resenas')
      .delete()
      .eq('id', reviewId)
      .eq('id_usuario', userId); 

    if (error) throw error;
    return true;
  } catch (err) {
    throw new Error('Error al eliminar reseña: ' + err.message);
  }
}

export async function toggleReviewLike(reviewId, userId) {
  try {
    
    const { data: existingLike, error: fetchError } = await supabase
      .from('resena_likes')
      .select('id')
      .eq('id_resena', reviewId)
      .eq('id_usuario', userId)
      .maybeSingle();

    if (fetchError) throw fetchError;

    if (existingLike) {
      
      const { error: deleteError } = await supabase
        .from('resena_likes')
        .delete()
        .eq('id', existingLike.id);
      
      if (deleteError) throw deleteError;
      return { action: 'removed' };

    } else {
      
      const { error: insertError } = await supabase
        .from('resena_likes')
        .insert({ id_resena: reviewId, id_usuario: userId });
      
      if (insertError) throw insertError;
      return { action: 'added' };
    }
  } catch (err) {
    throw new Error('Error en toggle like: ' + err.message);
  }
}

export async function updateReview(reviewId, userId, newRating, newComment) {
    try {
      const { error } = await supabase
        .from('resenas')
        .update({ calificacion: newRating, comentario: newComment })
        .eq('id', reviewId)
        .eq('id_usuario', userId); // Seguridad
  
      if (error) throw error;
      return true;
    } catch (err) {
      throw new Error('Error editando reseña: ' + err.message);
    }
}

// Función auxiliar privada para enriquecer reseñas de usuarios (poner nombre y foto del autor)
async function enrichUserReviews(reviews) {
  if (!reviews || reviews.length === 0) return [];

  const authorIds = [...new Set(reviews.map(r => r.id_autor))];

  
  const { data: authorsData, error } = await supabase
    .from('usuario')
    .select('id_usuario, nombre_usuario, foto')
    .in('id_usuario', authorIds);

  if (error) throw error;

  return reviews.map(r => {
    const author = authorsData.find(u => u.id_usuario === r.id_autor);
    return {
      id: String(r.id),
      targetUserId: String(r.id_destinatario),
      
      
      authorId: String(r.id_autor),
      authorName: author ? author.nombre_usuario : 'Usuario eliminado',
      authorPhoto: author ? author.foto : null,

      date: r.fecha,
      rating: Number(r.calificacion),
      comment: r.comentario || ''
    };
  });
}

// Crear una reseña a un usuario
export async function addUserReview({ id_autor, id_destinatario, calificacion, comentario }) {
  try {
    
    if (String(id_autor) === String(id_destinatario)) {
        throw new Error("No puedes reseñarte a ti mismo.");
    }

    const { data, error } = await supabase
      .from('resenas_usuarios') 
      .insert([{ id_autor, id_destinatario, calificacion, comentario }])
      .select()
      .single();

    if (error) throw error;
    return data;
  } catch (err) {
    throw new Error('Error creando reseña de usuario: ' + err.message);
  }
}

// Obtener reseñas que ha recibido un usuario (Para mostrar en su perfil)
export async function getReviewsForUser(userId) {
  try {
    const { data: reviews, error } = await supabase
      .from('resenas_usuarios')
      .select('*')
      .eq('id_destinatario', userId)
      .order('fecha', { ascending: false });

    if (error) throw error;
    
    
    return await enrichUserReviews(reviews);
  } catch (err) {
    throw new Error('Error obteniendo reseñas del usuario: ' + err.message);
  }
}

// Calcular promedio de calificación de usuario (Útil para la reputación visual)
export async function getUserRatingAverage(userId) {
    try {
        const { data, error } = await supabase
            .from('resenas_usuarios')
            .select('calificacion')
            .eq('id_destinatario', userId);
            
        if(error) throw error;
        
        if(!data || data.length === 0) return 0.0;
        
        const sum = data.reduce((acc, curr) => acc + Number(curr.calificacion), 0);
        return sum / data.length;
    } catch (err) {
        return 0.0;
    }
}

export async function deleteUserReview(reviewId, authorId) {
  try {
    const { error } = await supabase
      .from('resenas_usuarios') 
      .delete()
      .eq('id', reviewId)
      .eq('id_autor', authorId); 

    if (error) throw error;
    return true;
  } catch (err) {
    throw new Error('Error eliminando reseña de usuario: ' + err.message);
  }
}

export async function updateUserReview(reviewId, authorId, rating, comment) {
  try {
    const { error } = await supabase
      .from('resenas_usuarios') 
      .update({ calificacion: rating, comentario: comment })
      .eq('id', reviewId)
      .eq('id_autor', authorId); 

    if (error) throw error;
    return true;
  } catch (err) {
    throw new Error('Error actualizando reseña de usuario: ' + err.message);
  }
}

export async function getReviewBetweenUsers(authorId, targetUserId) {
  try {
    
    const { data, error } = await supabase
      .from('resenas_usuarios')
      .select('*')
      .eq('id_autor', authorId)
      .eq('id_destinatario', targetUserId)
      .maybeSingle(); 

    if (error) throw error;
    if (!data) return null;

    
    const enrichedList = await enrichUserReviews([data]);

    
    return enrichedList.length > 0 ? enrichedList[0] : null;

  } catch (err) {
    console.error('Error buscando reseña existente:', err);
    return null;
  }
}