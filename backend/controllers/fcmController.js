// controllers/fcmController.js
import { supabase } from "../lib/supabaseClient.js";

export const saveFcmToken = async (req, res) => {
  const userId = req.user.id_usuario; 
  const { token } = req.body;

  if (!token) {
    return res.status(400).json({ ok: false, error: "Falta token FCM" });
  }

  try {
    const { error } = await supabase
      .from("usuario")                  
      .update({ fcm_token: token })
      .eq("id_usuario", userId);         

    if (error) throw error;

    return res.json({ ok: true, message: "Token guardado correctamente" });
  } catch (err) {
    console.error("Error guardando FCM token:", err);
    return res.status(500).json({ ok: false, error: "No se pudo guardar el token" });
  }
};
