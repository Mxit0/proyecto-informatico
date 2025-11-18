import express from 'express';
import multer from 'multer';
import { 
  getAllProducts, 
  getProductById,
  createProduct, 
  uploadProductImages,
  getProductImages
} from '../repositories/productRepository.js';

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage() });

router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 100; 
    const products = await getAllProducts(page, limit); 
    res.json(products);
  } catch (error) {
    res.status(500).json({ error: 'Error al obtener productos: ' + error.message });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const product = await getProductById(id);
    if (!product) {
      return res.status(404).json({ error: 'Producto no encontrado' });
    }
    res.json(product);
  } catch (error) {
    res.status(500).json({ error: 'Error al obtener el producto: ' + error.message });
  }
});

router.post('/', async (req, res) => {
 try {
    const newProductData = req.body; 
    const { nombre, precio, descripcion, id_usuario, stock, categoria } = newProductData;

    if (!nombre || !precio || !descripcion || !id_usuario || !stock || !categoria) {
      return res.status(400).json({ 
        error: 'Datos incompletos. Se requieren: nombre, precio, descripcion, id_usuario, stock, categoria.' 
      });
    }
    newProductData.fecha_publicacion = new Date().toISOString();
    
    const createdProduct = await createProduct(newProductData);
    res.status(201).json(createdProduct); 
  
  } catch (error) {
    res.status(500).json({ error: 'Error al crear el producto: ' + error.message });
  }
});

router.post(
  '/:id/imagenes',
  upload.array('imagenes', 10), 
  async (req, res) => {
    try {
      const id_producto = req.params.id;
      const files = req.files;

      if (!files || files.length === 0) {
        return res.status(400).json({ error: 'No se enviaron archivos.' });
      }

      const urls = await uploadProductImages(id_producto, files);

      res.status(201).json({ 
        message: 'Imágenes subidas exitosamente', 
        urls: urls 
      });

    } catch (error) {
      res.status(500).json({ error: 'Error al subir imágenes: ' + error.message });
    }
  }
);

router.get('/:id/imagenes', async (req, res) => {
  try {
    const { id } = req.params;
    const images = await getProductImages(id);
    
    
    res.json(images || []); 

  } catch (error) {
    res.status(500).json({ error: 'Error al obtener las imágenes: ' + error.message });
  }
});

export default router;