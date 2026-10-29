import com.example.educacaocontinuadagamificada.recomendacao.application.RecomendacaoService;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.AssistentePort;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.IaIndisponivelException;
import com.example.educacaocontinuadagamificada.recomendacao.application.port.MotorRecomendacaoPort;
import com.example.educacaocontinuadagamificada.recomendacao.domain.Recomendacao;
import com.example.educacaocontinuadagamificada.recomendacao.domain.RespostaAssistente;
import com.example.educacaocontinuadagamificada.recomendacao.domain.ResultadoRecomendacao;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Harness do mini-lab Desafio 3 (roda sem Maven): usa o RecomendacaoService REAL do Core
 * com um adapter HTTP minimo (JDK HttpClient, mesmo timeout de 1500 ms do IaHttpAdapter)
 * apontando para o ia-service FastAPI REAL. Em producao o adapter e o IaHttpAdapter (Spring RestClient).
 */
public class LabIaIndisponivel {

    static final Duration TIMEOUT = Duration.ofMillis(1500);
    static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(TIMEOUT).build();
    static final Pattern ITEM = Pattern.compile("\"cursoId\":\"([^\"]+)\",\"titulo\":\"([^\"]+)\",\"score\":([0-9.]+),\"motivo\":\"([^\"]+)\"");

    static class AdapterLab implements MotorRecomendacaoPort, AssistentePort {
        final String base;
        AdapterLab(String base) { this.base = base; }

        public List<Recomendacao> recomendar(Long alunoId, int limite) {
            String corpo = get(base + "/recomendacoes/" + alunoId + "?limite=" + limite);
            List<Recomendacao> itens = new ArrayList<>();
            Matcher m = ITEM.matcher(corpo);
            while (m.find()) {
                itens.add(new Recomendacao(m.group(1), m.group(2), Double.parseDouble(m.group(3)), m.group(4)));
            }
            return itens;
        }

        public RespostaAssistente perguntar(String pergunta) {
            throw new UnsupportedOperationException("nao usado no lab");
        }

        private String get(String url) {
            try {
                HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).build(),
                        HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() >= 500) {
                    throw new IaIndisponivelException("HTTP " + r.statusCode(), null);
                }
                return r.body();
            } catch (java.io.IOException | InterruptedException e) {
                throw new IaIndisponivelException(e.getClass().getSimpleName(), e);
            }
        }
    }

    public static void main(String[] args) {
        RecomendacaoService service = new RecomendacaoService(new AdapterLab(args[0]), new AdapterLab(args[0]));
        long inicio = System.nanoTime();
        ResultadoRecomendacao r = service.recomendar(Long.parseLong(args[1]), 3);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        System.out.printf("  Core -> ia-service: degradado=%s origem=%s tempo=%dms itens=%s%n",
                r.degradado(), r.origem(), ms, r.itens().stream().map(Recomendacao::cursoId).toList());
    }
}
