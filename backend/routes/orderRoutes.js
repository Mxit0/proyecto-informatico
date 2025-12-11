import express from 'express';
import { getOrdersByUser, getOrderById } from '../repositories/orderRepository.js';
import { createOrderFromCart } from '../repositories/orderRepository.js';

const router = express.Router();

router.post('/', async (req, res) => {
  try {
    const { userId } = req.body;
    if (!userId) return res.status(400).json({ error: 'Falta userId' });

    const newOrderId = await createOrderFromCart(userId);
    
    res.status(201).json({ 
        message: 'Orden creada exitosamente', 
        orderId: newOrderId 
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

//FUNCIONES PARA EL HISTORIAL DE COMPRAS
// GET /api/orders/user/:userId
// Para ver la lista de todas las compras (Pantalla 1)
router.get('/user/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const orders = await getOrdersByUser(userId);
    res.json(orders);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET /api/orders/detail/:orderId
// Para ver qué productos había dentro de UNA compra (Pantalla 2)
router.get('/detail/:orderId', async (req, res) => {
  try {
    const { orderId } = req.params;
    const orderDetail = await getOrderById(orderId);
    
    if (!orderDetail) {
      return res.status(404).json({ error: 'Compra no encontrada' });
    }

    res.json(orderDetail);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;