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

    // test getParent() ------------------------------------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void testGetParent_NodeIsRoot() {
        root.getParent();
    }

    @Test
    public void testGetParent() {
        Node<Object> a = root.createChild('a');

        assertThat(a.getParent(), sameInstance(root));
    }
    // test getParent() ------------------------------------------------------------------------------------------------

    // test setSuffix() ------------------------------------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testSetSuffix_SuffixIsNull() {
        Node<Object> a = root.createChild('a');

        a.setSuffix(null);
    }

    @Test
    public void testSetSuffix() {
        Node<Object> a = root.createChild('a');

        a.setSuffix(node);

        assertThat(a.getSuffix(), sameInstance(node));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSuffix_SuffixIsNull() {
        Node<Object> a = root.createChild('a');

        a.getSuffix();
    }
    // test setSuffix() ------------------------------------------------------------------------------------------------

    // test setTerminalSuffix() ----------------------------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testSetTerminalSuffix_TerminalSuffixIsNull() {
        Node<Object> a = root.createChild('a');

        a.setTerminalSuffix(null);
    }

    @Test
    public void testSetTerminalSuffix() {
        Node<Object> a = root.createChild('a');

        a.setTerminalSuffix(node);

        assertThat(a.getTerminalSuffix(), sameInstance(node));
    }
    // test setTerminalSuffix() ----------------------------------------------------------------------------------------


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

    @Test
    public void testCompact() {
        root.setChildren(children);

        root.compact();

        verify(children, times(1)).compact();
        verifyNoMoreInteractions(children);
    }

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

    // test setChildren() ----------------------------------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testSetChildren_ChildrenIsNull() {
        root.setChildren(null);
    }

    @Test
    public void testSetChildren() {
        assertThat(root.getChildren(), nullValue());

        root.setChildren(children);

        assertThat(root.getChildren(), sameInstance(children));
    }
    // test setChildren() ----------------------------------------------------------------------------------------------

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