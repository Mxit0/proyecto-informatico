// backend/server.js

const express = require('express');
const dotenv = require('dotenv');
//const { Client } = require('pg');
const { supabase } = require('./supabaseClient.js');
// Carga las variables del archivo .env
dotenv.config();

const app = express();
// Lee el puerto desde el .env, si no existe usa 3000
const PORT = process.env.PORT || 3000; 

// Conexión a Supabase (PostgreSQL)
/*const dbClient = new Client({
    // Lee la URL de conexión desde .env (DATABASE_URL)
    connectionString: process.env.DATABASE_URL,
});*/

// Middleware para JSON
app.use(express.json());

// --- Ruta de Prueba de Estado ---
app.get('/', (req, res) => {
    res.status(200).json({ 
        status: 'Online', 
        message: 'API de Compraventa de Electrónicos en línea y funcionando.'
    });
});

// --- Ruta de Prueba de Conexión a Base de Datos ---
app.get('/api/v1/db-status', async (req, res) => {
    try {
        
        
        
        const { data, error } = await supabase
            .from('carrito') 
            .select('id') 
            .limit(1);    
        if (error) {
            throw error;
        }
        res.status(200).json({ status: 'Connected', client: 'Supabase JS Client' });
        
    } catch (error) {
        console.error('Error al conectar a Supabase (API):', error.message);
        res.status(500).json({ status: 'Error', message: 'Fallo al conectar a Supabase', error: error.message });
    }
});

// Iniciar el servidor
app.listen(PORT, () => {
    console.log(`Servidor de API corriendo en http://localhost:${PORT}`);
});