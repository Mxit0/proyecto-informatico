import express from 'express';
import multer from 'multer';
import { 
  getAllProducts, 
  createProduct, 
  uploadProductImages 
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

router.get('/tipos', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('componente_maestro')
      .select('categoria');

    if (error) throw error;

    const tipos = [...new Set(data.map(d => d.categoria))];

    res.json(tipos);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/:tipo', async (req, res) => {
  const { tipo } = req.params;

  try {
    const { data, error } = await supabase
      .from('componente_maestro')
      .select('id, nombre_componente, categoria')
      .eq('categoria', tipo);

    if (error) throw error;

    res.json(data);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.post('/', async (req, res) => {
  try {
    const { 
      id_componente_maestro,
      precio,
      descripcion,
      id_usuario,
      stock
    } = req.body;

    if (!id_componente_maestro || !precio || !id_usuario) {
      return res.status(400).json({
        error: "Faltan datos requeridos: id_componente_maestro, precio, id_usuario"
      });
    }

    // 1. Obtener maestro para autocompletar nombre + categoria
    const { data: maestro, error: maestroErr } = await supabase
      .from('componente_maestro')
      .select('nombre_componente, categoria')
      .eq('id', id_componente_maestro)
      .single();

    if (maestroErr || !maestro) {
      return res.status(400).json({ error: "Componente maestro inválido" });
    }

    // 2. Crear producto final
    const newProduct = {
      nombre: maestro.nombre_componente,   // autocompletado
      categoria: maestro.categoria,        // autocompletado
      descripcion: descripcion || '',
      precio,
      id_usuario,
      stock: stock ?? 1,
      id_componente_maestro
    };

    const created = await createProduct(newProduct);
    res.json(created);

  } catch (error) {
    res.status(500).json({ error: error.message });
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

export default router;