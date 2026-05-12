package com.org.catolicasc.AuditoriaIso25010.service.relatorio;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.org.catolicasc.AuditoriaIso25010.model.ResultadoModulo;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioService {

    // Marcação: Geração do arquivo PDF em memória
    public byte[] gerarRelatorioPdf(List<ResultadoModulo> resultados) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("Relatório de Auditoria - ISO/IEC 25010", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);

            // Marcação: Iteração sobre os resultados para compor o sumário executivo
            for (ResultadoModulo resultado : resultados) {
                adicionarSecaoModulo(document, resultado);
            }

            document.close();
        } catch (DocumentException e) {
            // Marcação: Captura de erro na montagem do PDF
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }

        return out.toByteArray();
    }

    // Marcação: Formatação individual de cada módulo no documento
    private void adicionarSecaoModulo(Document document, ResultadoModulo resultado) throws DocumentException {
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font fontTexto = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font fontDestaque = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        Paragraph subtitulo = new Paragraph(resultado.getNomeModulo(), fontSubtitulo);
        subtitulo.setSpacingBefore(15);
        subtitulo.setSpacingAfter(10);
        document.add(subtitulo);

        String statusTexto = resultado.isAprovado() ? "APROVADO" : "REPROVADO";
        Paragraph status = new Paragraph("Status: " + statusTexto, fontDestaque);
        document.add(status);

        Paragraph observacao = new Paragraph("Parecer: " + resultado.getObservacao(), fontTexto);
        observacao.setSpacingAfter(10);
        document.add(observacao);

        if (resultado.getMetricas() != null && !resultado.getMetricas().isEmpty()) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            PdfPCell cellChave = new PdfPCell(new Phrase("Métrica", fontDestaque));
            PdfPCell cellValor = new PdfPCell(new Phrase("Valor Obtido", fontDestaque));
            table.addCell(cellChave);
            table.addCell(cellValor);

            for (Map.Entry<String, Object> metrica : resultado.getMetricas().entrySet()) {
                table.addCell(new Phrase(metrica.getKey(), fontTexto));
                table.addCell(new Phrase(String.valueOf(metrica.getValue()), fontTexto));
            }

            document.add(table);
        }
    }
}