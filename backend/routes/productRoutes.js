// Archivo: routes/productRoutes.js
import express from 'express';
const router = express.Router();

import { getAllProducts, createProduct } from '../repositories/productRepository.js';

/*
 * @ruta   GET /productos
 */
router.get('/', async (req, res) => {
  try {
   
    // Si no vienen, usamos 1 y 100 por defecto.
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 100; 

    // Pasamos estos valores al repositorio
    const products = await getAllProducts(page, limit); 
    res.json(products);
  } catch (error) {
    res.status(500).json({ error: 'Error al obtener productos: ' + error.message });
  }
});

/*
 * @ruta   POST /productos
 */
router.post('/', async (req, res) => {
 try {
    const newProductData = req.body; 

    
    const { nombre, precio, descripcion, id_usuario, stock, categoria } = newProductData;

    if (!nombre || !precio || !descripcion || !id_usuario || !stock || !categoria) {
      return res.status(400).json({ 
        error: 'Datos incompletos. Se requieren: nombre, precio, descripcion, id_usuario, stock, categoria.' 
      });
    }
    //insertar fecha de publicacion del producto
    newProductData.fecha_publicacion = new Date().toISOString();
    
    const createdProduct = await createProduct(newProductData);
    res.status(201).json(createdProduct); 
  
  } catch (error) {
    res.status(500).json({ error: 'Error al crear el producto: ' + error.message });
  }
});

export default router;