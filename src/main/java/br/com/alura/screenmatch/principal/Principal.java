package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);

    private ConsumoApi consumoApi = new ConsumoApi();

    ConverteDados converteDados = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";

    private final String API_KEY = "&apikey=6a4094cc";

    public void exibeMenu() {
        System.out.println("Digite o nome da série :");
        var nomeSerie = leitura.nextLine();

        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = converteDados.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = converteDados.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        temporadas.forEach(System.out::println);

        temporadas.forEach(temp -> temp.episodios().forEach(ep -> System.out.println(ep.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream().flatMap(t -> t.episodios().stream()).collect(Collectors.toList());

        System.out.println("\n top 5 melhores episodios");
        dadosEpisodios.stream().filter(e -> !e.avaliacao().equalsIgnoreCase("n/a")).sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed()).limit(5).forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d)))
                        .collect(Collectors.toList());

        episodios.forEach(System.out::println);

//        System.out.println("Digite o titulo de um episodio :");
//        var trechoTitulo = leitura.nextLine();
//        Optional<Episodio> episodioBuscado = episodios.stream().filter(e -> e.getTitulo().toLowerCase().contains(trechoTitulo.toLowerCase())).findFirst();
//
//        if (episodioBuscado.isPresent()) {
//            System.out.println("Episodio encontrado!");
//            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
//        }else {
//            System.out.println("episodio nao encontrado");
//        }

//        System.out.println("A partir de que ano voce deseja ver os episodios ?");
//        var ano = leitura.nextInt();
//        leitura.nextLine();

//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//        episodios.stream()
//                .filter(e -> e.getDataDeLancamento() != null && e.getDataDeLancamento().isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                " episodio: " + e.getNumeroEpisodio() +
//                                " Data de lancamento: " + e.getDataDeLancamento().format(formatter)
//                ));

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors
                        .groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println(est);
    }
}
