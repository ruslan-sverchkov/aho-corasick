package org.rsverchk.ahocorasick;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link MutableTrie} class.
 * The test doesn't look good and it actually is shitty which can point to the fact that the code itself is not ideal.
 * // todo do something with it when inspiration comes in
 *
 * @author Ruslan Sverchkov
 */
@RunWith(MockitoJUnitRunner.class)
public class MutableTrieTest {

    @Mock
    private CharConverter converter;

    @Mock
    private MatchHandler<Object> handler;

    @Mock
    private NodeConsumer<Object> consumer;

    @Mock
    private Node<Object> node;

    @Mock
    private Node<Object> suffix;

    @Mock
    private Node<Object> terminalSuffix;

    @Mock
    private Object payload;

    private Node<Object> root;
    private Node<Object> a;
    private Node<Object> b;
    private Node<Object> c;
    private Node<Object> ab;
    private Node<Object> abc;

    private MutableTrie<Object> trie;

    @Before
    public void setUp() {
        root = Node.root();

        root.setSuffix(root);

        // level 1 --------------------
        a = root.createChild('a');
        b = root.createChild('b');
        c = root.createChild('c');

        a.setSuffix(root);
        b.setSuffix(root);
        c.setSuffix(root);

        c.setPayload("c");
        // level 1 --------------------

        // level 2 --------------------
        ab = a.createChild('b');
        ab.setSuffix(b);
        // level 2 --------------------

        // level 3 --------------------
        abc = ab.createChild('c');
        abc.setSuffix(c);
        abc.setPayload("abc");
        abc.setTerminalSuffix(c);
        // level 3 --------------------

        trie = spy(new MutableTrie<>(converter));

        trie.setRoot(root);
    }

    // test addCharSequence() ------------------------------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testAddCharSequence_SequenceIsNull() {
        trie.addCharSequence(null, payload);
    }

    @Test(expected = NullPointerException.class)
    public void testAddCharSequence_PayloadIsNull() {
        trie.addCharSequence("text", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddCharSequence_SequenceInEmpty() {
        trie.addCharSequence("", payload);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddCharSequence_InvalidState() {
        trie.setBuilt(true);
        trie.addCharSequence("text", payload);
    }

    @Test
    public void testAddCharSequence() {
        doReturn('a').when(converter).convert('a');
        doReturn('b').when(converter).convert('b');
        doReturn('c').when(converter).convert('c');
        doReturn('d').when(converter).convert('d');

        trie.addCharSequence("abcd", payload);

        assertThat(trie.getRoot(), sameInstance(root));

        assertThat(root.getChildren().size(), equalTo(3));
        assertThat(root.getChild('a'), sameInstance(a));
        assertThat(root.getChild('b'), sameInstance(b));
        assertThat(root.getChild('c'), sameInstance(c));

        assertThat(a.getChildren().size(), equalTo(1));
        assertThat(a.getChild('b'), sameInstance(ab));
        assertThat(b.getChildren(), nullValue());
        assertThat(c.getChildren(), nullValue());

        assertThat(ab.getChildren().size(), equalTo(1));
        assertThat(ab.getChild('c'), sameInstance(abc));

        assertThat(abc.getChildren().size(), equalTo(1));
        Node<Object> d = abc.getChild('d');
        assertThat(d.getPayload(), sameInstance(payload));
    }
    // test addCharSequence() ------------------------------------------------------------------------------------------

    // test init() -----------------------------------------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void testInit_InvalidState() {
        trie.setBuilt(true);
        trie.init();
    }

    @Test
    public void testInit() {
        doReturn(consumer).when(trie).getNodeInitializer();
        doNothing().when(trie).breadthFirstTraversal(consumer);
        trie.setRoot(node);

        trie.init();

        assertThat(trie.isBuilt(), is(true));
        verify(node, times(1)).init();
        verify(trie, times(1)).breadthFirstTraversal(consumer);
        verifyNoMoreInteractions(node);
    }
    // test init() -----------------------------------------------------------------------------------------------------

    // test match() ----------------------------------------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testMatch_SequenceInNull() {
        trie.match(null, handler);
    }

    @Test(expected = NullPointerException.class)
    public void testMatch_HandlerInNull() {
        trie.match("text", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMatch_SequenceInEmpty() {
        trie.match("", handler);
    }

    @Test(expected = IllegalStateException.class)
    public void testMatch_InvalidState() {
        trie.match("text", handler);
    }

    @Test
    public void testMatch_Interrupt() {
        doReturn('a').when(converter).convert('a');
        doReturn('b').when(converter).convert('b');
        doReturn('c').when(converter).convert('c');
        doReturn('$').when(converter).convert('$');

        trie.setBuilt(true);

        doReturn(true).when(trie).handleMatch(a, 0, handler);
        doReturn(true).when(trie).handleMatch(ab, 1, handler);
        doReturn(true).when(trie).handleMatch(abc, 2, handler);
        doReturn(false).when(trie).handleMatch(b, 4, handler);
        doReturn(true).when(trie).handleMatch(c, 5, handler);

        trie.match("abc$bc", handler);

        InOrder inOrder = inOrder(trie);
        inOrder.verify(trie, times(1)).handleMatch(a, 0, handler);
        inOrder.verify(trie, times(1)).handleMatch(ab, 1, handler);
        inOrder.verify(trie, times(1)).handleMatch(abc, 2, handler);
        inOrder.verify(trie, times(1)).handleMatch(b, 4, handler);
        inOrder.verify(trie, never()).handleMatch(c, 5, handler);
    }

    @Test
    public void testMatch() {
        doReturn('a').when(converter).convert('a');
        doReturn('b').when(converter).convert('b');
        doReturn('c').when(converter).convert('c');
        doReturn('$').when(converter).convert('$');

        trie.setBuilt(true);

        doReturn(true).when(trie).handleMatch(a, 0, handler);
        doReturn(true).when(trie).handleMatch(ab, 1, handler);
        doReturn(true).when(trie).handleMatch(abc, 2, handler);
        doReturn(true).when(trie).handleMatch(b, 4, handler);
        doReturn(true).when(trie).handleMatch(c, 5, handler);

        trie.match("abc$bc", handler);

        InOrder inOrder = inOrder(trie);
        inOrder.verify(trie, times(1)).handleMatch(a, 0, handler);
        inOrder.verify(trie, times(1)).handleMatch(ab, 1, handler);
        inOrder.verify(trie, times(1)).handleMatch(abc, 2, handler);
        inOrder.verify(trie, times(1)).handleMatch(b, 4, handler);
        inOrder.verify(trie, times(1)).handleMatch(c, 5, handler);
    }
    // test match() ----------------------------------------------------------------------------------------------------

    // test breadthFirstTraversal() ------------------------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testBreadthFirstTraversal_ConsumerIsNull() {
        trie.breadthFirstTraversal(null);
    }

    @Test
    public void testBreadthFirstTraversal() {
        trie.breadthFirstTraversal(consumer);

        InOrder inOrder = inOrder(consumer);

        inOrder.verify(consumer, times(1)).consume('a', a);
        inOrder.verify(consumer, times(1)).consume('c', c); // remember there is hashing
        inOrder.verify(consumer, times(1)).consume('b', b);
        inOrder.verify(consumer, times(1)).consume('b', ab);
        inOrder.verify(consumer, times(1)).consume('c', abc);
        verifyNoMoreInteractions(consumer);
    }
    // test breadthFirstTraversal() ------------------------------------------------------------------------------------

    // test handleMatch() ----------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testHandleMatch_IndexIsLesserThanNodeLevel() {
        trie.handleMatch(ab, 0, handler);
    }

    @Test(expected = NullPointerException.class)
    public void testHandleMatch_HandlerIsNull() {
        trie.handleMatch(a, 1, null);
    }

    @Test
    public void testHandleMatch_Interrupt() {
        doReturn(false).when(handler).handle(0, 3, "abc");
        doReturn(true).when(handler).handle(2, 3, "c");

        trie.handleMatch(abc, 2, handler);

        verify(handler, times(1)).handle(0, 3, "abc");
        verify(handler, never()).handle(2, 3, "c");
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void testHandleMatch() {
        doReturn(true).when(handler).handle(0, 3, "abc");
        doReturn(true).when(handler).handle(2, 3, "c");

        trie.handleMatch(abc, 2, handler);

        verify(handler, times(1)).handle(0, 3, "abc");
        verify(handler, times(1)).handle(2, 3, "c");
        verifyNoMoreInteractions(handler);
    }
    // test handleMatch() ----------------------------------------------------------------------------------------------

    @Test
    public void testGetNodeInitializer() {
        NodeConsumer<Object> nodeInitializer = trie.getNodeInitializer();

        nodeInitializer.consume('$', node);

        verify(node, times(1)).init();
        verifyNoMoreInteractions(node);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_ConverterIsNull() {
        new MutableTrie<>(null);
    }

    // black box -------------------------------------------------------------------------------------------------------
    @Test
    public void blackBoxTest() {
        MutableTrie<String> trie = new MutableTrie(c -> c);
        trie.addCharSequence("hers", "hers");
        trie.addCharSequence("his", "his");
        trie.addCharSequence("sher", "sher");
        trie.addCharSequence("he", "he");
        trie.init();

        Node<String> root = trie.getRoot();
        checkRoot(root, 2);

        // level 1 ------------------------------------------------
        Node<String> h = root.getChild('h');
        checkNode(h, root, root, null, false, null, 1, 2);

        Node<String> s = root.getChild('s');
        checkNode(s, root, root, null, false, null, 1, 1);
        // level 1 ------------------------------------------------

        // level 2 ------------------------------------------------
        Node<String> he = h.getChild('e');
        checkNode(he, h, root, null, true, "he", 2, 1);

        Node<String> hi = h.getChild('i');
        checkNode(hi, h, root, null, false, null, 2, 1);

        Node<String> sh = s.getChild('h');
        checkNode(sh, s, h, null, false, null, 2, 1);
        // level 2 ------------------------------------------------

        // level 3 ------------------------------------------------
        Node<String> her = he.getChild('r');
        checkNode(her, he, root, null, false, null, 3, 1);

        Node<String> his = hi.getChild('s');
        checkNode(his, hi, s, null, true, "his", 3, 0);

        Node<String> she = sh.getChild('e');
        checkNode(she, sh, he, he, false, null, 3, 1);
        // level 3 ------------------------------------------------

        // level 4 ------------------------------------------------
        Node<String> hers = her.getChild('s');
        checkNode(hers, her, s, null, true, "hers", 4, 0);

        Node<String> sher = she.getChild('r');
        checkNode(sher, she, her, null, true, "sher", 4, 0);
        // level 4 ------------------------------------------------
    }

    private void checkRoot(Node<String> root, int numberOfChildren) {

        assertThat(root.isRoot(), is(true));
        assertThat(root.isTerminal(), is(false));
        assertThat(root.getSuffix(), sameInstance(root));
        assertThat(root.getTerminalSuffix(), nullValue());
        assertThat(root.getLevel(), equalTo(0));
        assertThat(root.getChildren().size(), is(numberOfChildren));
    }

    private void checkNode(Node<String> node,
                           Node<String> parent,
                           Node<String> suffix,
                           Node<String> terminalSuffix,
                           boolean isTerminal,
                           String payload,
                           int level,
                           int numberOfChildren) {
        assertThat(node.getParent(), sameInstance(parent));
        assertThat(node.getSuffix(), sameInstance(suffix));
        if (terminalSuffix == null) {
            assertThat(node.getTerminalSuffix(), nullValue());
        } else {
            assertThat(node.getTerminalSuffix(), sameInstance(terminalSuffix));
        }
        assertThat(node.isTerminal(), is(isTerminal));
        if (payload == null) {
            assertThat(node.getPayload(), nullValue());
        } else {
            assertThat(node.getPayload(), sameInstance(payload));
        }
        assertThat(node.getLevel(), equalTo(level));
        if (numberOfChildren == 0) {
            assertThat(node.getChildren(), nullValue());
        } else {
            assertThat(node.getChildren().size(), is(numberOfChildren));
        }
    }
    // black box -------------------------------------------------------------------------------------------------------

}