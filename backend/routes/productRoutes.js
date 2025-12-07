import express from 'express';
import multer from 'multer';
import { supabase } from "../lib/supabaseClient.js";
/*import { 
  getAllProducts, 
  getProductById,
  createProduct, 
  uploadProductImages,
  getProductImages,
  updateProduct
} from '../repositories/productRepository.js';*/
import productRepository2 from '../repositories/productRepository2.js';


const router = express.Router();
const upload = multer({ storage: multer.memoryStorage() });


console.log("CARGANDO productRoutes.js CORRECTO");
router.get('/debug', (req, res) => {
  res.json({ message: "Ruta debug funcionando" });
});

// ======================
//  GET - Todos los productos
// ======================
router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 100;

    const products = await productRepository2.getAllProducts(page, limit);
    res.json(products);

  } catch (error) {
    res.status(500).json({ error: 'Error al obtener productos: ' + error.message });
  }
});


// ======================
//  GET - Tipos de componente
// ======================
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

// ======================
//  GET - Componentes por tipo
// ======================
router.get('/tipos/:tipo', async (req, res) => {
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


// ======================
//  GET - Producto por ID
// ======================
router.get('/id/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const product = await productRepository2.getProductById(id);

    if (!product) {
      return res.status(404).json({ error: 'Producto no encontrado' });
    }

    res.json(product);

  } catch (error) {
    res.status(500).json({ error: 'Error al obtener el producto: ' + error.message });
  }
});


// ======================
//  POST - Crear nuevo producto (con componente maestro)
// ======================
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

    // Obtener maestro
    const { data: maestro, error: maestroErr } = await supabase
      .from('componente_maestro')
      .select('nombre_componente, categoria')
      .eq('id', id_componente_maestro)
      .single();

    if (maestroErr || !maestro) {
      return res.status(400).json({ error: "Componente maestro inválido" });
    }

    // Crear producto final
    const newProduct = {
      nombre: maestro.nombre_componente,
      categoria: maestro.categoria,
      descripcion: descripcion || '',
      precio,
      id_usuario,
      stock: stock ?? 1,
      id_componente_maestro
    };

    const created = await productRepository2.createProduct(newProduct);
    res.json(created);

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;