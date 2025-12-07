import { supabase } from '../lib/supabaseClient.js';


const TABLE_PRODUCTO = 'producto'; 
console.log("productRepository: supabase =", supabase);
export const productRepository = {

  // 1. Obtener todos los productos con paginación
  async getAllProducts(page = 1, limit = 10) {
    try {
     const from = (page - 1) * limit;
     const to = from + limit - 1;

     const { data, error } = await supabase
.from(TABLE_PRODUCTO)
.select(`
id,
nombre, 
descripcion, 
precio, 
stock, 
categoria, 
fecha_publicacion,

-- JOIN CRÍTICO (Sintaxis limpia de PostgREST)
id_componente_maestro (categoria, especificaciones),

-- JOIN a usuario
usuario:id_usuario (nombre_usuario, correo, reputacion)
`)
.range(from, to)
.order('fecha_publicacion', { ascending: false });

if (error) throw error;

// Post-procesamiento para aplanar la respuesta y obtener specs
const processedData = (data || []).map(item => {
const masterComponent = item.id_componente_maestro;

// Obtenemos la categoría STRING y las especificaciones del componente maestro
const masterCategory = masterComponent ? masterComponent.categoria : null;
const masterSpecs = masterComponent ? masterComponent.especificaciones : null;

return {
...item,
// Usamos 'categoria' para el valor del maestro para que el compatibilityService lo reconozca
categoria: masterCategory, 
especificaciones: masterSpecs,
id_componente_maestro: undefined, // Limpiamos el objeto anidado
};
});

return processedData;

} catch (err) {
throw err;
}
},

// 2. Obtener producto por ID (Método crucial para compatibilityService)
async getProductById(productId) {
try {
const { data, error } = await supabase
.from(TABLE_PRODUCTO)
.select(`
nombre,
id_componente_maestro (categoria, especificaciones),
id, precio, stock, categoria, id_usuario
`)
.eq('id', productId)
.single();

if (error) throw error;
if (!data) return null;

const masterComponent = data.id_componente_maestro;

if (!masterComponent) {
// Mantener la robustez para que no falle si la relación es nula
    console.error(`⚠️ FALLO DE JOIN: El producto ${productId} no tiene un id_componente_maestro válido.`);
    return null; 
}

// Devolvemos el objeto aplanado con los campos esperados por el compatibilityService
return {
    id: data.id,
    nombre: data.nombre, 
    categoria: masterComponent.categoria, // Categoría STRING del maestro
    especificaciones: masterComponent.especificaciones, // Specs JSONB del maestro
    precio: data.precio,
    stock: data.stock,
    id_usuario: data.id_usuario
};

} catch (err) {
    throw err;
    }
},

// 3. Obtener múltiples productos por IDs
async getProductsByIds(productIds) {
    if (!productIds || productIds.length === 0) {
    return [];
}

try {
const { data, error } = await supabase
.from(TABLE_PRODUCTO)
.select(`
    id,
    nombre,
    precio,
    stock,
    id_componente_maestro (categoria, especificaciones), -- JOIN CRÍTICO LIMPIO
    usuario:id_usuario (nombre_usuario, correo, reputacion) -- JOIN a usuario
    `)
.in('id', productIds);

if (error) throw error;

// Post-procesamiento para aplanar la respuesta y obtener specs
const processedData = (data || []).map(item => {
const masterComponent = item.id_componente_maestro;
const masterCategory = masterComponent ? masterComponent.categoria : null;
const masterSpecs = masterComponent ? masterComponent.especificaciones : null;

    return {
    ...item,
    categoria: masterCategory, 
    especificaciones: masterSpecs,
    id_componente_maestro: undefined,
    };
});

return processedData;

} catch (err) {
    throw err;
    }
},


async createProduct(productData) {
    const { 
        nombre, 
        descripcion, 
        precio, 
        id_usuario, 
        stock, 
        categoria, 
        id_componente_maestro 
    } = productData;

const { data, error } = await supabase
    .from(TABLE_PRODUCTO) 
    .insert([{ 
        nombre, 
        descripcion, 
        precio, 
        id_usuario,
        stock, 
        categoria, 
        id_componente_maestro
        }])
        .select()
        .single();

if (error) throw error;
    return data;
}
};

export default productRepository;