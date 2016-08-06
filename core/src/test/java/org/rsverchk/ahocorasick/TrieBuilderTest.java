package org.rsverchk.ahocorasick;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link TrieBuilder} class.
 *
 * @author Ruslan Sverchkov
 */
@RunWith(MockitoJUnitRunner.class)
public class TrieBuilderTest {

    @Mock
    private Object payload;

    @Mock
    private MutableTrie<Object> trie;

    @Mock
    private Trie<Object> immutableTrie;

    @Mock
    private CharConverter emptyConverter;

    @Mock
    private CharConverter toLowerCaseConverter;

    private TrieBuilder<Object> builder;

    @Before
    public void setUp() {
        builder = spy(new TrieBuilder<>());

        doReturn(emptyConverter).when(builder).createEmptyConverter();
        doReturn(toLowerCaseConverter).when(builder).createToLowerCaseConverter();
        doReturn(trie).when(builder).createMutableTrie(emptyConverter);
        doReturn(immutableTrie).when(builder).createImmutableTrie(trie);
    }

    // test withConverter() --------------------------------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void testWithConverter_IllegalState() {
        builder.setTrie(trie);

        builder.withConverter(emptyConverter);
    }

    @Test
    public void testWithConverter() {
        doReturn(trie).when(builder).createMutableTrie(toLowerCaseConverter);

        assertThat(builder.withConverter(toLowerCaseConverter), sameInstance(builder));
        assertThat(builder.getTrie(), sameInstance(trie));
    }
    // test withConverter() --------------------------------------------------------------------------------------------

    // test ignoreCase() -----------------------------------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void testIgnoreCase_IllegalState() {
        builder.setTrie(trie);

        builder.ignoreCase();
    }

    @Test
    public void testIgnoreCase() {
        doReturn(trie).when(builder).createMutableTrie(toLowerCaseConverter);

        assertThat(builder.ignoreCase(), sameInstance(builder));
        assertThat(builder.getTrie(), sameInstance(trie));
    }
    // test ignoreCase() -----------------------------------------------------------------------------------------------

    // test addCharSequence() ------------------------------------------------------------------------------------------
    @Test
    public void testAddCharSequence_NoTrie() {
        builder.setTrie(null);

        assertThat(builder.addCharSequence("text", payload), sameInstance(builder));

        verify(trie, times(1)).addCharSequence("text", payload);
        verifyNoMoreInteractions(trie);
    }

    @Test
    public void testAddCharSequence() {
        builder.setTrie(trie);

        assertThat(builder.addCharSequence("text", payload), sameInstance(builder));

        verify(trie, times(1)).addCharSequence("text", payload);
        verifyNoMoreInteractions(trie);
    }
    // test addCharSequence() ------------------------------------------------------------------------------------------

    // test build() ----------------------------------------------------------------------------------------------------
    @Test
    public void testBuild_NoTrie() {
        builder.setTrie(null);

        assertThat(builder.build(), sameInstance(immutableTrie));

        verify(trie, times(1)).init();
        verifyNoMoreInteractions(trie);
    }

    @Test
    public void testBuild() {
        builder.setTrie(trie);

        assertThat(builder.build(), sameInstance(immutableTrie));

        verify(trie, times(1)).init();
        verifyNoMoreInteractions(trie);
    }
    // test build() ----------------------------------------------------------------------------------------------------

    @Test
    public void testCreateToLowerCaseConverter() {
        doCallRealMethod().when(builder).createToLowerCaseConverter();

        assertThat(builder.createToLowerCaseConverter().convert('C'), equalTo('c'));
    }

    @Test
    public void testCreateEmptyConverter() {
        doCallRealMethod().when(builder).createEmptyConverter();

        assertThat(builder.createEmptyConverter().convert('C'), equalTo('C'));
    }

    @Test
    public void testCreateMutableTrie() {
        doCallRealMethod().when(builder).createMutableTrie(toLowerCaseConverter);

        assertThat(builder.createMutableTrie(toLowerCaseConverter), notNullValue());
    }

    @Test
    public void testCreateImmutableTrie() {
        doCallRealMethod().when(builder).createImmutableTrie(trie);

        assertThat(builder.createImmutableTrie(trie), notNullValue());
    }

}