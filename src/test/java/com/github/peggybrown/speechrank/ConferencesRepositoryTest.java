package com.github.peggybrown.speechrank;

import com.github.peggybrown.speechrank.dto.ConferenceDto;
import com.github.peggybrown.speechrank.dto.ConferenceImportDto;
import com.github.peggybrown.speechrank.dto.YearDto;
import com.github.peggybrown.speechrank.entity.Comment;
import com.github.peggybrown.speechrank.entity.Conference;
import com.github.peggybrown.speechrank.entity.Presentation;
import com.github.peggybrown.speechrank.entity.Rate;
import com.github.peggybrown.speechrank.gateway.Importer;
import javaslang.collection.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

public class ConferencesRepositoryTest {

    @Test
    public void should_import_all_conferences() {
        //given
        Importer importer = Mockito.mock(Importer.class);
        List<Importer.VideoData> lists = List.empty();
        when(importer.importDevConf2017()).thenReturn(lists);
        when(importer.importDevConf2019()).thenReturn(lists);
        when(importer.importBoilingFrogs2018()).thenReturn(
            List.of(aVideo("001", "JUnit 5", "What is new JUniy 5")));
        when(importer.importBoilingFrogs2019()).thenReturn(lists);
        when(importer.importScalar2019()).thenReturn(lists);
        when(importer.importConfitura2018()).thenReturn(lists);
        when(importer.importConfitura2019()).thenReturn(lists);

        ConferencesRepository repository = new ConferencesRepository(importer);
        //when
        repository.importAllConferences();

        //then
        assertThat(repository.getConferences().size()).isEqualTo(7);
        assertThat(repository.getConferences().iterator()).extracting(
            Conference::getId, Conference::getName, c -> c.getPresentations().size()
        ).containsExactlyInAnyOrder(
            tuple("11", "DevConf", 0),
            tuple("12", "DevConf", 0),
            tuple("21", "Boiling Frogs", 1),
            tuple("31", "Boiling Frogs", 0),
            tuple("41", "Scalar", 0),
            tuple("51", "Confitura", 0),
            tuple("51", "Confitura", 0)
        );
        assertThat(repository.getYears().iterator()).extracting(
            YearDto::getYear, y -> y.getConferences().size(),
            y -> y.getConferences().size() > 0 ? y.getConferences().get(0).getName() : null,
            y -> y.getConferences().size() > 1 ? y.getConferences().get(1).getName() : null,
            y -> y.getConferences().size() > 2 ? y.getConferences().get(2).getName() : null,
            y -> y.getConferences().size() > 3 ? y.getConferences().get(3).getName() : null
        ).containsExactlyInAnyOrder(
            tuple("2017", 1, "DevConf", null, null, null),
            tuple("2018", 2, "Boiling Frogs", "Confitura", null, null),
            tuple("2019", 4, "DevConf", "Boiling Frogs", "Scalar", "Confitura")
        );
        assertThat(repository.getConference("12"))//
            .extracting(
                ConferenceDto::getId, ConferenceDto::getName, ConferenceDto::getPresentations)
            .containsExactly(
                "12", "DevConf", Collections.emptyList()
            );
        assertThat(repository.getConference("21")//
            ).extracting(
                ConferenceDto::getId,
                ConferenceDto::getName,
                c -> c.getPresentations().size(),
                c -> c.getPresentations().get(0).getTitle()
            ).containsExactly(
                "21", "Boiling Frogs", 1, "JUnit 5"
            );
    }

    @Test
    public void shoult_import_conference() {
        //given
        Importer importer = Mockito.mock(Importer.class);
        String playlistId = "PLVbNBx5Phg3AwVti8rYNqx222pgfMZWO";
        when(importer.importFromYouTubePlaylist(playlistId)).thenReturn(List.of(
             aVideo("001", "TestNG", "What's new TestNG"),
             aVideo("002", "Mockito", "What's new Mockito")
        ));
        ConferencesRepository repository = new ConferencesRepository(importer);
        ConferenceImportDto conf = aConference(playlistId);
        //when
        repository.importConference(conf);
        //then
        assertThat(repository.getConferences().size()).isEqualTo(1);
        assertThat(repository.getConferences().iterator()).extracting(
            Conference::getName,
            c -> c.getPresentations().size(),
            c -> c.getPresentations().size() > 0 ? c.getPresentations().get(0).getTitle() : null,
            c -> c.getPresentations().size() > 1 ? c.getPresentations().get(1).getTitle() : null
        ).containsExactlyInAnyOrder(
            tuple( "Greenfield", 2, "TestNG", "Mockito")
        );
    }

    @Test
    public void should_add_rate_for_presentation() {
        //given
        Context c = prepareConferencesRepository();
        String presentationId = c.presentationJava11.getId();
        Rate rate = aRate(presentationId, 10);
        assertThat(c.presentationJava11.getRating()).isEqualTo(0);
        //when
        c.repository.add(rate);
        //then
        assertThat(c.presentationJava11.getRating()).isEqualTo(10.0);
        assertThat(c.presentationJava11.getRates().toJavaList())
            .containsExactly(aRate(presentationId, 10));
    }

    @Test
    public void should_add_comment_for_presentation() {
        //given
        Context c = prepareConferencesRepository();
        String presentationId = c.presentationJava11.getId();
        Comment comment = aComment(presentationId, "Ok !!!");
        assertThat(c.presentationJava11.getComments().size()).isEqualTo(0);
        //when
        c.repository.add(comment);
        //then
        assertThat(c.presentationJava11.getComments().toJavaList())
            .containsExactly(aComment(presentationId, "Ok !!!"));
    }

    private static class Context {

        public ConferencesRepository repository;
        public Conference conferenceGreenfield;
        public Presentation presentationJava11;
        public Presentation presentationJava12;

    }


    private Context prepareConferencesRepository() {
        Context result = new Context();
        Importer importer = Mockito.mock(Importer.class);
        Importer.VideoData vD1 = aVideo("001", "Java 11", "What's new Java 11");
        Importer.VideoData vD2 = aVideo("002", "Java 12", "What's new Java 12");
        String playlistId = "PLVbNBx5Phg3AwVti8rYNqx222pgfMZWO";

        when(importer.importFromYouTubePlaylist(playlistId)).thenReturn(List.of(vD1, vD2));

        result.repository = new ConferencesRepository(importer);

        ConferenceImportDto conf = aConference(playlistId);

        result.repository.importConference(conf);

        result.conferenceGreenfield = result.repository.getConferences().get(0);
        result.presentationJava11 = result.conferenceGreenfield.getPresentations().get(0);
        result.presentationJava12 = result.conferenceGreenfield.getPresentations().get(1);

        return result;
    }
    private ConferenceImportDto aConference(String playlistId) {
        ConferenceImportDto conf = new ConferenceImportDto();
        conf.setName("Greenfield");
        conf.setYear("2019");
        conf.setPlaylistLink(playlistId);
        return conf;
    }

    private Comment aComment(String presentationId, String comment) {
        Comment result = new Comment();
        result.setUsername("Jan Kowalski");
        result.setPresentationId(presentationId);
        result.setUserId("0008");
        result.setComment(comment);
        return result;
    }

    private Rate aRate(String presentationId, int rate) {
        Rate aRate = new Rate();
        aRate.setUserId("0001");
        aRate.setPresentationId(presentationId);
        aRate.setRate(rate);
        return aRate;
    }

    private Importer.VideoData aVideo(String videoId, String title, String description) {
        return new Importer.VideoData(videoId, title, description);
    }

}
