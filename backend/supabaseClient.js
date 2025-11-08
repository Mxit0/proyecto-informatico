// supabaseClient.js
const { createClient } = require("@supabase/supabase-js"); 
const dotenv = require("dotenv");
dotenv.config();

const supabaseUrl = process.env.SUPABASE_URL;
const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!supabaseUrl || !supabaseServiceKey) {
  throw new Error("Falta SUPABASE_URL o SUPABASE_SERVICE_ROLE_KEY en .env");
}

const supabase = createClient(supabaseUrl, supabaseServiceKey);

// CAMBIADO: de 'export' a 'module.exports'
module.exports = { supabase };
