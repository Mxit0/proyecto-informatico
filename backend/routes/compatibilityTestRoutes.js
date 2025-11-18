import express from 'express';
import productRepository2 from '../repositories/productRepository2.js';

const router = express.Router();

// Ruta para crear productos de prueba
router.post('/seed-products', async (req, res) => {
  try {
    // Aquí puedes llamar a tu función de seed
    res.json({ success: true, message: 'Productos de prueba creados' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Ruta para obtener productos de la nueva tabla
router.get('/products', async (req, res) => {
  try {
    const { page = 1, limit = 10, category } = req.query;
    const products = await productRepository2.getAllProducts(parseInt(page), parseInt(limit));
    res.json(products);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;