import express from 'express';
import { 
  addToCart, 
  getCartByUser, 
  removeItemFromCart,
  clearCart
} from '../repositories/carroRepository.js';

const router = express.Router();

// GET /api/cart/:userId
router.get('/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const cartData = await getCartByUser(userId);
    res.json(cartData);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST /api/cart
// JSON Body: { "userId": 1, "productId": 10, "quantity": 1 }
router.post('/', async (req, res) => {
  try {
    const { userId, productId, quantity } = req.body;

    if (!userId || !productId || !quantity) {
      return res.status(400).json({ error: 'Faltan datos requeridos' });
    }

    const result = await addToCart(userId, productId, quantity);
    res.status(201).json({ message: 'Producto agregado', data: result });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE /api/cart/:userId/item/:productId
router.delete('/:userId/item/:productId', async (req, res) => {
  try {
    const { userId, productId } = req.params;
    await removeItemFromCart(userId, productId);
    res.json({ message: 'Item eliminado' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE /api/cart/:userId (Vaciar todo)
router.delete('/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    await clearCart(userId);
    res.json({ message: 'Carrito vaciado' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;