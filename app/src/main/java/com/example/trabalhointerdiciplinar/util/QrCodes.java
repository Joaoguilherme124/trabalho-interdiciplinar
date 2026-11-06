package com.example.trabalhointerdiciplinar.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

/** Gera o bitmap de um QR Code (ZXing). */
public final class QrCodes {
    private QrCodes() {
    }

    /** @return o bitmap, ou null se o conteúdo não puder ser codificado */
    public static Bitmap gerar(String conteudo, int tamanhoPx, int corModulo, int corFundo) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);
        try {
            BitMatrix matriz = new QRCodeWriter()
                    .encode(conteudo, BarcodeFormat.QR_CODE, tamanhoPx, tamanhoPx, hints);
            int[] pixels = new int[tamanhoPx * tamanhoPx];
            for (int y = 0; y < tamanhoPx; y++) {
                for (int x = 0; x < tamanhoPx; x++) {
                    pixels[y * tamanhoPx + x] = matriz.get(x, y) ? corModulo : corFundo;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(tamanhoPx, tamanhoPx, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, tamanhoPx, 0, 0, tamanhoPx, tamanhoPx);
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }

    public static Bitmap gerarEmPretoEBranco(String conteudo, int tamanhoPx) {
        return gerar(conteudo, tamanhoPx, Color.BLACK, Color.WHITE);
    }
}
