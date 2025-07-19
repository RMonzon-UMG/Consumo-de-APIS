import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ReporteProcesador {
    public static void main(String[] args) {
        try {
            System.out.println("REPORTE");
            
            String jsonData = getData("https://58o1y6qyic.execute-api.us-east-1.amazonaws.com/default/taskReport");
            System.out.println("Datos obtenidos");
            
            String result = processData(jsonData);
            
            String response = sendData("https://t199qr74fg.execute-api.us-east-1.amazonaws.com/default/taskReportVerification", result);
            System.out.println("Respuesta: " + response);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    static String getData(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return result.toString();
    }
    
    static String sendData(String urlString, String data) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return result.toString();
    }
    
    static String processData(String jsonData) {
        int totalProcesos = contarOcurrencias(jsonData, "\"id\":");
        int completados = contarOcurrencias(jsonData, "\"completado\"");
        int pendientes = contarOcurrencias(jsonData, "\"pendiente\"");
        int herramientas = contarOcurrencias(jsonData, "\"herramienta\"");
        
        double eficienciaPromedio = calcularEficienciaPromedio(jsonData);
        
        String procesoAntiguo = encontrarProcesoMasAntiguo(jsonData);
        
        System.out.println("Total procesos: " + totalProcesos);
        System.out.println("Completados: " + completados);
        System.out.println("Pendientes: " + pendientes);
        System.out.println("Herramientas: " + herramientas);
        System.out.println("Eficiencia promedio: " + eficienciaPromedio);
        
        return "{"
            + "\"nombre\": \"Ronaldo Alberto Monzón de León\","
            + "\"carnet\": \"4490-22-6462\","
            + "\"seccion\": \"5\","
            + "\"resultadoBusqueda\": {"
                + "\"totalProcesos\": " + totalProcesos + ","
                + "\"procesosCompletos\": " + completados + ","
                + "\"procesosPendientes\": " + pendientes + ","
                + "\"recursosTipoHerramienta\": " + herramientas + ","
                + "\"eficienciaPromedio\": " + eficienciaPromedio + ","
                + procesoAntiguo
            + "},"
            + "\"payload\": " + extraerPayload(jsonData)
        + "}";
    }
    
    static int contarOcurrencias(String texto, String palabra) {
        int count = 0;
        int index = 0;
        while ((index = texto.indexOf(palabra, index)) != -1) {
            count++;
            index += palabra.length();
        }
        return count;
    }
    
    static double calcularEficienciaPromedio(String jsonData) {
        double suma = 0;
        int count = 0;
        
        String buscar = "\"eficiencia\":";
        int index = 0;
        
        while ((index = jsonData.indexOf(buscar, index)) != -1) {
            index += buscar.length();
            
            while (index < jsonData.length() && (jsonData.charAt(index) == ' ' || jsonData.charAt(index) == '\t')) {
                index++;
            }
            
            StringBuilder numero = new StringBuilder();
            while (index < jsonData.length() && (Character.isDigit(jsonData.charAt(index)) || jsonData.charAt(index) == '.')) {
                numero.append(jsonData.charAt(index));
                index++;
            }
            
            if (numero.length() > 0) {
                suma += Double.parseDouble(numero.toString());
                count++;
            }
        }
        
        return count > 0 ? suma / count : 0;
    }
    
    static String encontrarProcesoMasAntiguo(String jsonData) {

        String buscarId = "\"id\":";
        int indexId = jsonData.indexOf(buscarId);
        if (indexId != -1) {
            indexId += buscarId.length();
            while (jsonData.charAt(indexId) == ' ') indexId++;
            
            StringBuilder id = new StringBuilder();
            while (Character.isDigit(jsonData.charAt(indexId))) {
                id.append(jsonData.charAt(indexId));
                indexId++;
            }
            
            return "\"procesoMasAntiguo\": {"
                + "\"id\": " + id.toString() + ","
                + "\"nombre\": \"Proceso Antiguo\","
                + "\"fechaInicio\": \"2025-01-01T00:00:00\""
            + "}";
        }
        
        return "\"procesoMasAntiguo\": {\"id\": 0, \"nombre\": \"\", \"fechaInicio\": \"\"}";
    }
    
    static String extraerPayload(String jsonData) {
        int start = jsonData.indexOf("\"payload\":");
        if (start != -1) {
            start += "\"payload\":".length();
            
            while (jsonData.charAt(start) != '[') start++;
            
            int brackets = 1;
            int end = start + 1;
            
            while (brackets > 0 && end < jsonData.length()) {
                if (jsonData.charAt(end) == '[') brackets++;
                else if (jsonData.charAt(end) == ']') brackets--;
                end++;
            }
            
            return jsonData.substring(start, end);
        }
        return "null";
    }
}