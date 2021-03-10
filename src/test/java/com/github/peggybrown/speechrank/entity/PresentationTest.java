package com.github.peggybrown.speechrank.entity;

import com.github.peggybrown.speechrank.gateway.Importer;
import javaslang.collection.List;
import junit.framework.TestCase;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.Test;

import static org.assertj.core.groups.Tuple.*;

public class PresentationTest  {

    @Test
    public void should_create_presentation() {
        //when
        Presentation presentation = new Presentation(aVideoData());
        //then
        Assertions.assertThat(presentation.getComments()).isNotNull().hasSize(0);
        Assertions.assertThat(presentation.getRates()).isNotNull().hasSize(0);
        Assertions.assertThat(presentation.getId()).isNotNull();
        Assertions.assertThat(presentation).extracting(//
            Presentation::getLink, //
            Presentation::getRating, //
            Presentation::getTitle).//
            containsExactly(//
            "https://youtube.com/embed/01" , //
            0.0,
            "JUtit5"
        );
    }

    @Test
    public void should_calculate_rating_after_add_rate() {
        //given
        Presentation presentation = new Presentation(aVideoData());
        presentation.addRate(aRate(3));
        Assertions.assertThat(presentation.getRating()).isEqualTo(3);
        //when
        presentation.addRate(aRate(6));
        //then
        Assertions.assertThat(presentation.getRating()).isEqualTo(4.5);
    }

    @Test
    public void should_add_rate() {
        //given
        Presentation presentation = new Presentation(aVideoData());
        Assertions.assertThat(presentation.getRating()).isEqualTo(0);
        //when
        presentation.addRate(aRate(6));
        //then
        Assertions.assertThat(presentation.getRates()).hasSize(1).containsExactly(aRate(6));
    }

    @Test
    public void should_add_comment() {
        //given
        Presentation presentation = new Presentation(aVideoData());
        Comment comment = new Comment();
        comment.setPresentationId(presentation.getId());
        comment.setUserId("01");
        comment.setUsername("Janek");
        comment.setComment("Polecam!");
        Assertions.assertThat(presentation.getComments()).hasSize(0);
        //when
        presentation.addComment(comment);
        //then
        Assertions.assertThat(presentation.getComments()).hasSize(1).extracting(
            Comment::getUserId, Comment::getUsername, Comment::getComment
        ).containsExactly(
            tuple("01", "Janek", "Polecam!")
        );
    }


    private Rate aRate(int value) {
        Rate rate = new Rate();
        rate.setRate(value);
        return rate;
    }

    private Importer.VideoData aVideoData() {
        return new Importer.VideoData("01","JUtit5","Prezentacja nowych możliwości JUtit5");
    }
}
