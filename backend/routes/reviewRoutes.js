import express from 'express';
import { 
  addReview, 
  getReviewsByProduct, 
  getReviewsWrittenByUser, 
  getReviewsReceivedBySeller 
} from '../repositories/reviewRepository.js';

const router = express.Router();

// Sirve para: Obtener las reseñas de un producto
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

// Sirve para: Obtener el historial de reseñas escritas por un usuario
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

// Sirve para: Obtener las reseñas recibidas por un vendedor (Reputación)
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

// Sirve para: Publicar una nueva reseña
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
    // Si ya existe reseña, devolvemos error 409 (Conflicto)
    if (error.message.includes('Ya has calificado')) {
      return res.status(409).json({ error: error.message });
    }
    res.status(500).json({ error: error.message });
  }
});

export default router;