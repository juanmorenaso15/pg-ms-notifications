package com.pulse_gym.ms_notifications.util;

/**
 * Utilidades para normalizar numeros de telefono al formato que exigen
 * tanto la API de WhatsApp Cloud (Meta) como WhatsApp Web: solo digitos,
 * con indicativo de pais y sin "+".
 */
public final class TelefonoUtils {

    private TelefonoUtils() {
    }

    /**
     * Normaliza un numero de telefono. El perfil de usuario no obliga a
     * guardar el indicativo de pais, asi que si el numero quedo en formato
     * local (10 digitos) se le antepone el indicativo por defecto.
     *
     * @param telefono           Numero de telefono a normalizar
     * @param defaultCountryCode Indicativo a anteponer si el numero viene sin el
     * @return Numero de telefono normalizado (solo digitos, con indicativo)
     */
    public static String normalizar(String telefono, String defaultCountryCode) {
        String soloDigitos = telefono.replaceAll("[^0-9]", "");
        if (soloDigitos.length() == 10) {
            return defaultCountryCode + soloDigitos;
        }
        return soloDigitos;
    }
}
