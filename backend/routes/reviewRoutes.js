import express from 'express';
import { 
  addReview, 
  getReviewsByProduct, 
  getReviewsWrittenByUser, 
  getReviewsReceivedBySeller,
  deleteReview,        
  toggleReviewLike,    
  updateReview,
  addUserReview,
  deleteUserReview,
  updateUserReview,
  getReviewsForUser,
  getUserRatingAverage,
  getReviewBetweenUsers
} from '../repositories/reviewRepository.js';

const router = express.Router();

//Obtener las reseñas de un producto
// URL: GET /api/reviews/product/5
router.get('/product/:productId', async (req, res) => {
  try {
    const { productId } = req.params;
    const reviews = await getReviewsByProduct(productId);
    res.json(reviews);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Obtener el historial de reseñas escritas por un usuario
// URL: GET /api/reviews/user/2/history
router.get('/user/:userId/history', async (req, res) => {
  try {
    const { userId } = req.params;
    const reviews = await getReviewsWrittenByUser(userId);
    res.json(reviews);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Obtener las reseñas recibidas por un vendedor (Reputación)
// URL: GET /api/reviews/seller/1
router.get('/seller/:sellerId', async (req, res) => {
  try {
    const { sellerId } = req.params;
    const reviews = await getReviewsReceivedBySeller(sellerId);
    res.json(reviews);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET /api/reviews/user/check
router.get('/user/check', async (req, res) => {
  try {
    const { authorId, targetId } = req.query;
    if (!authorId || !targetId) return res.status(400).json({ error: 'Faltan IDs' });

    const review = await getReviewBetweenUsers(authorId, targetId);
    res.json({ exists: !!review, review: review });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Publicar una nueva reseña
// URL: POST /api/reviews
router.post('/', async (req, res) => {
  try {
    const { productId, userId, rating, comment } = req.body;

    // Validación básica de datos
    if (!productId || !userId || !rating) {
      return res.status(400).json({ error: 'Faltan datos requeridos (productId, userId, rating)' });
    }

    const newReview = await addReview({
      id_producto: productId,
      id_usuario: userId,
      calificacion: rating,
      comentario: comment
    });

    res.status(201).json(newReview);

  } catch (error) {
    
    if (error.message.includes('Ya has calificado')) {
      return res.status(409).json({ error: error.message });
    }
    res.status(500).json({ error: error.message });
  }
});

router.delete('/:reviewId', async (req, res) => {
    try {
        const { reviewId } = req.params;
        const { userId } = req.body; // Enviado desde el frontend

        if (!userId) return res.status(400).json({ error: 'Falta userId' });

        await deleteReview(reviewId, userId);
        res.json({ message: 'Reseña eliminada' });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

router.post('/:reviewId/like', async (req, res) => {
    try {
        const { reviewId } = req.params;
        const { userId } = req.body;

        if (!userId) return res.status(400).json({ error: 'Falta userId' });

        const result = await toggleReviewLike(reviewId, userId);
        res.json(result); // { action: 'added' } o { action: 'removed' }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

router.put('/:reviewId', async (req, res) => {
    try {
        const { reviewId } = req.params;
        const { userId, rating, comment } = req.body;

        if (!userId) return res.status(400).json({ error: 'Falta userId' });

        await updateReview(reviewId, userId, rating, comment);
        res.json({ message: 'Reseña actualizada' });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// POST /api/reviews/user -> Crear reseña a un vendedor
router.post('/user', async (req, res) => {
  try {
    const { authorId, targetUserId, rating, comment } = req.body;

    if (!authorId || !targetUserId || !rating) {
      return res.status(400).json({ error: 'Faltan datos (authorId, targetUserId, rating)' });
    }

    const result = await addUserReview({
      id_autor: authorId,
      id_destinatario: targetUserId,
      calificacion: rating,
      comentario: comment
    });

    res.status(201).json(result);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET /api/reviews/user/:userId -> Ver las reseñas de un vendedor
router.get('/user/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const reviews = await getReviewsForUser(userId);
    res.json(reviews);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET /api/reviews/user/:userId/average -> Obtener promedio simple (Opcional)
router.get('/user/:userId/average', async (req, res) => {
  try {
    const { userId } = req.params;
    const average = await getUserRatingAverage(userId);
    res.json({ average });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE /api/reviews/user/:reviewId
router.delete('/user/:reviewId', async (req, res) => {
  try {
    const { reviewId } = req.params;
    // BUSCAMOS EN QUERY O BODY (Prioridad a Query para DELETE)
    const userId = req.query.userId || req.body.userId; 

    if (!userId) return res.status(400).json({ error: 'Falta userId (query param)' });

    await deleteUserReview(reviewId, userId);
    res.json({ message: 'Reseña de usuario eliminada' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// PUT /api/reviews/user/:reviewId
router.put('/user/:reviewId', async (req, res) => {
  try {
    const { reviewId } = req.params;
    const { userId, rating, comment } = req.body;

    if (!userId) return res.status(400).json({ error: 'Falta userId' });

    await updateUserReview(reviewId, userId, rating, comment);
    res.json({ message: 'Reseña de usuario actualizada' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});



export default router;