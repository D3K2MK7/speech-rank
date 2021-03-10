package com.github.peggybrown.speechrank.entity;


import javaslang.collection.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;

public class YearTest {

    @Test
    public void should_add_conference() {
        //given
        Year year = new Year("2021");
        Conference conf = new Conference("10", "JUG", List.empty());
        //when
        year.addConference(conf);
        //then
        Assertions.assertThat(year.getConferences()).hasSize(1).containsExactly(conf);
    }

}
