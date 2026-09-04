package com.peihua.dragswap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val CONTAINER = "container"
private const val CELL_DP = 60
private const val COLUMNS = 3
private const val LIST_WIDTH_DP = 200

@RunWith(AndroidJUnit4::class)
class DragSwapBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    /** 用户报的核心缺陷：第3个拖到第1个应当与第1个对调，第2个必须原地不动。 */
    @Test
    fun swapMode_gridThirdOntoFirst_swapsOnlyThoseTwo() {
        val items = mutableStateListOf("A", "B", "C", "D", "E", "F", "G", "H", "I")
        rule.setContent { GridLayout(items, DragMode.Swap) }

        longPressDrag(cellCenter(2), cellCenter(0))

        assertEquals(listOf("C", "B", "A", "D", "E", "F", "G", "H", "I"), items.toList())
    }

    /** 对照组：同一手势在 Reorder 模式下应是插入平移，确认两种模式确实不同。 */
    @Test
    fun reorderMode_gridThirdOntoFirst_shiftsItems() {
        val items = mutableStateListOf("A", "B", "C", "D", "E", "F", "G", "H", "I")
        rule.setContent { GridLayout(items, DragMode.Reorder) }

        longPressDrag(cellCenter(2), cellCenter(0))

        assertEquals(listOf("C", "A", "B", "D", "E", "F", "G", "H", "I"), items.toList())
    }

    @Test
    fun swapMode_columnThirdOntoFirst_swapsOnlyThoseTwo() {
        val items = mutableStateListOf("A", "B", "C")
        rule.setContent { ListLayout(items, DragMode.Swap) }

        longPressDrag(rowCenter(2), rowCenter(0))

        assertEquals(listOf("C", "B", "A"), items.toList())
    }

    /** 拖到空白处松手不应产生任何变更。 */
    @Test
    fun swapMode_dropOnEmptySpace_keepsOrder() {
        val items = mutableStateListOf("A", "B", "C")
        rule.setContent { GridLayout(items, DragMode.Swap) }

        longPressDrag(cellCenter(0), cellCenter(7))

        assertEquals(listOf("A", "B", "C"), items.toList())
    }

    private fun longPressDrag(start: Offset, end: Offset) {
        rule.onNodeWithTag(CONTAINER).performTouchInput { down(start) }
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithTag(CONTAINER).performTouchInput {
            advanceEventTime(1_000)
            moveTo(start + Offset(2f, 2f))
            advanceEventTime(16)
            moveTo(end)
            advanceEventTime(16)
            up()
        }
        rule.waitForIdle()
    }

    private fun cellCenter(index: Int): Offset {
        val cell = with(rule.density) { CELL_DP.dp.toPx() }
        return Offset(
            cell * (index % COLUMNS) + cell / 2,
            cell * (index / COLUMNS) + cell / 2,
        )
    }

    private fun rowCenter(index: Int): Offset {
        val cell = with(rule.density) { CELL_DP.dp.toPx() }
        val width = with(rule.density) { LIST_WIDTH_DP.dp.toPx() }
        return Offset(width / 2, cell * index + cell / 2)
    }

    @Composable
    private fun GridLayout(items: SnapshotStateList<String>, mode: DragMode) {
        val gridState = rememberLazyGridState()
        val dragState = rememberDragSwapGridState(gridState, mode) { from, to ->
            items.applyDragMove(mode, from, to)
        }
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(COLUMNS),
            modifier = Modifier
                .size((CELL_DP * COLUMNS).dp)
                .testTag(CONTAINER)
                .dragSwapContainer(dragState),
        ) {
            items(items, key = { it }) { item ->
                DragSwapItem(dragState, item, Modifier.size(CELL_DP.dp)) { _, _ ->
                    Box(Modifier.fillMaxSize().background(Color.Gray))
                }
            }
        }
    }

    @Composable
    private fun ListLayout(items: SnapshotStateList<String>, mode: DragMode) {
        val listState = rememberLazyListState()
        val dragState = rememberDragSwapListState(listState, mode) { from, to ->
            items.applyDragMove(mode, from, to)
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .size(LIST_WIDTH_DP.dp)
                .testTag(CONTAINER)
                .dragSwapContainer(dragState),
        ) {
            items(items, key = { it }) { item ->
                DragSwapItem(
                    dragState,
                    item,
                    Modifier.fillMaxWidth().height(CELL_DP.dp),
                ) { _, _ ->
                    Box(Modifier.fillMaxSize().background(Color.Gray))
                }
            }
        }
    }
}
