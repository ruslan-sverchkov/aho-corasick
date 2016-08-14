package org.rsverchk.ahocorasick;

import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.procedure.TCharObjectProcedure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link Node} class.
 *
 * @author Ruslan Sverchkov
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeTest {

    @Mock
    private MatchHandler<Object> handler;

    @Mock
    private TCharObjectProcedure<Node<Object>> procedure;

    @Mock
    private TCharObjectHashMap<Node<Object>> children;

    @Mock
    private Object payload;

    @Mock
    private Node<Object> node;

    private Node<Object> root;

    @Before
    public void setUp() {
        root = Node.root();
    }

    @Test
    public void testIsRoot() {
        assertThat(root.isRoot(), is(true));

        Node<Object> a = root.createChild('a');

        assertThat(a.isRoot(), is(false));
    }

    @Test
    public void testIsTerminal() {
        Node<Object> a = root.createChild('a');

        assertThat(a.isTerminal(), is(false));

        a.setPayload(payload);

        assertThat(a.isTerminal(), is(true));
    }

    // test setPayload() -----------------------------------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testSetPayload_PayloadIsNull() {
        Node<Object> a = root.createChild('a');

        a.setPayload(null);
    }

    @Test
    public void testSetPayload() {
        Node<Object> a = root.createChild('a');

        a.setPayload(payload);

        assertThat(a.getPayload(), sameInstance(payload));
    }
    // test setPayload() -----------------------------------------------------------------------------------------------

    // test createChild() ----------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testCreateChild_AlreadyExists() {
        root.createChild('a');
        root.createChild('a');
    }

    @Test
    public void testCreateChild() {
        Node<Object> a = root.createChild('a');

        assertThat(a.getParent(), sameInstance(root));
        assertThat(a.getLevel(), equalTo(1));
        assertThat(root.getChild('a'), sameInstance(a));
    }
    // test createChild() ----------------------------------------------------------------------------------------------

    @Test
    public void testGetChild() {
        Node<Object> a = root.createChild('a');
        Node<Object> b = root.createChild('b');
        Node<Object> c = root.createChild('c');

        assertThat(root.getChild('a'), equalTo(a));
        assertThat(root.getChild('b'), equalTo(b));
        assertThat(root.getChild('c'), equalTo(c));

        assertThat(root.getChild('d'), nullValue());
    }

    // test forEachChild() ---------------------------------------------------------------------------------------------
    @Test
    public void testForEachChild_NoChildren() {
        root.forEachChild(procedure);

        verifyNoMoreInteractions(procedure);
    }

    @Test
    public void testForEachChild() {
        Node<Object> a = root.createChild('a');
        Node<Object> b = root.createChild('b');
        Node<Object> c = root.createChild('c');

        doReturn(true).when(procedure).execute('a', a);
        doReturn(true).when(procedure).execute('b', b);
        doReturn(true).when(procedure).execute('c', c);

        root.forEachChild(procedure);

        verify(procedure, times(1)).execute('a', a);
        verify(procedure, times(1)).execute('b', b);
        verify(procedure, times(1)).execute('c', c);
        verifyNoMoreInteractions(procedure);
    }
    // test forEachChild() ---------------------------------------------------------------------------------------------

    // test handleMatch() ----------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testHandleMatch_IndexIsLesserThanNodeLevel() {
        Node<Object> a = root.createChild('a');
        Node<Object> ab = a.createChild('b');

        ab.handleMatch(0, handler);
    }

    @Test(expected = NullPointerException.class)
    public void testHandleMatch_HandlerIsNull() {
        Node<Object> a = root.createChild('a');

        a.handleMatch(1, null);
    }

    @Test
    public void testHandleMatch_Interrupt() {
        Node<Object> a = root.createChild('a');
        Node<Object> ab = a.createChild('b');
        Node<Object> abc = ab.createChild('c');
        Node<Object> c = root.createChild('c');

        root.setSuffix(root);
        a.setSuffix(root);
        c.setSuffix(root);
        c.setPayload("c");
        ab.setSuffix(root);
        abc.setSuffix(c);
        abc.setTerminalSuffix(c);
        abc.setPayload("abc");

        doReturn(false).when(handler).handle(0, 3, "abc");
        doReturn(true).when(handler).handle(2, 3, "c");

        abc.handleMatch(2, handler);

        verify(handler, times(1)).handle(0, 3, "abc");
        verify(handler, never()).handle(2, 3, "c");
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void testHandleMatch() {
        Node<Object> a = root.createChild('a');
        Node<Object> ab = a.createChild('b');
        Node<Object> abc = ab.createChild('c');
        Node<Object> c = root.createChild('c');

        root.setSuffix(root);
        a.setSuffix(root);
        c.setSuffix(root);
        c.setPayload("c");
        ab.setSuffix(root);
        abc.setSuffix(c);
        abc.setTerminalSuffix(c);
        abc.setPayload("abc");

        doReturn(true).when(handler).handle(0, 3, "abc");
        doReturn(true).when(handler).handle(2, 3, "c");

        abc.handleMatch(2, handler);

        verify(handler, times(1)).handle(0, 3, "abc");
        verify(handler, times(1)).handle(2, 3, "c");
        verifyNoMoreInteractions(handler);
    }
    // test handleMatch() ----------------------------------------------------------------------------------------------

    @Test
    public void testFindTerminal() {
        Node<Object> root = Node.root();
        root.setSuffix(root);

        assertThat(root.findTerminalSuffix(), nullValue());

        // level 1 -----------------------------------------
        Node<Object> a = root.createChild('a');
        Node<Object> b = root.createChild('b');
        Node<Object> c = root.createChild('c');

        a.setSuffix(root);
        b.setSuffix(root);
        c.setSuffix(root);

        b.setPayload(new Object());

        assertThat(a.findTerminalSuffix(), nullValue());
        assertThat(b.findTerminalSuffix(), nullValue());
        assertThat(c.findTerminalSuffix(), nullValue());
        // level 1 -----------------------------------------

        // level 2 -----------------------------------------
        Node<Object> ab = a.createChild('b');
        Node<Object> cd = c.createChild('d');

        ab.setSuffix(b);
        cd.setSuffix(root);

        assertThat(ab.findTerminalSuffix(), sameInstance(b));
        assertThat(cd.findTerminalSuffix(), nullValue());
        // level 2 -----------------------------------------
    }

    @Test
    public void testFindSuffix() {
        Node<Object> root = Node.root();
        root.setSuffix(root);

        // level 1 -----------------------------------------
        Node<Object> a = root.createChild('a');
        Node<Object> b = root.createChild('b');
        Node<Object> c = root.createChild('c');

        assertThat(a.findSuffix(), sameInstance(root));
        assertThat(b.findSuffix(), sameInstance(root));
        assertThat(c.findSuffix(), sameInstance(root));

        a.setSuffix(root);
        b.setSuffix(root);
        c.setSuffix(root);
        // level 1 -----------------------------------------

        // level 2 -----------------------------------------
        Node<Object> ab = a.createChild('b');
        Node<Object> cd = c.createChild('d');

        assertThat(ab.findSuffix(), sameInstance(b));
        assertThat(cd.findSuffix(), sameInstance(root));

        ab.setSuffix(b);
        cd.setSuffix(root);
        // level 2 -----------------------------------------

        // level 3 -----------------------------------------
        Node<Object> abc = ab.createChild('c');
        Node<Object> cde = cd.createChild('e');

        assertThat(abc.findSuffix(), sameInstance(c));
        assertThat(cde.findSuffix(), sameInstance(root));
        // level 3 -----------------------------------------
    }

    // test initNode() -------------------------------------------------------------------------------------------------
//    @Test
//    public void testInitNode_NoTerminalSuffix() {
//        doReturn(suffix).when(trie).findSuffix('$', node);
//        doReturn(null).when(trie).findTerminalSuffix(node);
//
//        trie.initNode('$', node);
//
//        verify(node, times(1)).setSuffix(suffix);
//        verify(node, times(1)).compact();
//        verifyNoMoreInteractions(node);
//    }
//
//    @Test
//    public void testInitNode() {
//        doReturn(suffix).when(trie).findSuffix('$', node);
//        doReturn(terminalSuffix).when(trie).findTerminalSuffix(node);
//
//        trie.initNode('$', node);
//
//        InOrder inOrder = inOrder(node);
//        inOrder.verify(node, times(1)).setSuffix(suffix);
//        inOrder.verify(node, times(1)).setTerminalSuffix(terminalSuffix);
//        inOrder.verify(node, times(1)).compact();
//        verifyNoMoreInteractions(node);
//    }
    // test initNode() -------------------------------------------------------------------------------------------------

    // test getKey() ---------------------------------------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void testGetKey_RootNode() {
        root.getKey();
    }

    @Test
    public void testGetKey() {
        Node<Object> a = root.createChild('a');
        Node<Object> ab = a.createChild('b');
        Node<Object> ac = a.createChild('c');

        assertThat(a.getKey(), equalTo('a'));
        assertThat(ab.getKey(), equalTo('b'));
        assertThat(ac.getKey(), equalTo('c'));
    }
    // test getKey() ---------------------------------------------------------------------------------------------------

    // test getChildKey() ----------------------------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testGetChildKey_NoSuchChild() {
        root.createChild('a');

        root.getChildKey(Node.root());
    }

    @Test
    public void testGetChildKey() {
        Node<Object> a = root.createChild('a');
        Node<Object> ab = a.createChild('b');
        Node<Object> ac = a.createChild('c');

        assertThat(a.getChildKey(ab), equalTo('b'));
        assertThat(a.getChildKey(ac), equalTo('c'));
    }
    // test getChildKey() ----------------------------------------------------------------------------------------------

    @Test
    public void testToString() {
        Node<Object> a = root.createChild('a');
        Node<Object> ab = a.createChild('b');
        Node<Object> abc = ab.createChild('c');

        assertThat(abc.toString(), notNullValue()); // make sure there are no exceptions or infinite loops
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ParentIsNull_LevelIsNegative() {
        new Node<>(null, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ParentIsNull_LevelIsPositive() {
        new Node<>(null, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ParentIsNotNull_LevelIsNegative() {
        new Node<>(root, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ParentIsNotNull_LevelIsZero() {
        new Node<>(root, 0);
    }

}