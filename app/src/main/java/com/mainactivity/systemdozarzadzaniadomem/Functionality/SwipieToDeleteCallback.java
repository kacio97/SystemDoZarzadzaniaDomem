package com.mainactivity.systemdozarzadzaniadomem.Functionality;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mainactivity.systemdozarzadzaniadomem.R;

/**
 * klasa ta służy do bsługi zachowań usuwania elementów z recycleView.
 *
 * Klasa rozszerzona o ItemTouchHelper.Callback - klasa jest używana głównie do przeciągania i upuszczania oraz swipe'owania
 */
public class SwipieToDeleteCallback extends ItemTouchHelper.Callback {

    Context context;
    private Paint clearPaint;
    private ColorDrawable background;
    private int backgroundColor;
    private Drawable deleteDrawable;
    private int intrinsicWidth;
    private int intrinsicHeight;

    public SwipieToDeleteCallback(Context context) {
        this.context = context;
        background = new ColorDrawable();
        backgroundColor = Color.parseColor("#ff3b3b");
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        deleteDrawable = ContextCompat.getDrawable(context, R.drawable.bin_resized);

        intrinsicHeight = deleteDrawable.getIntrinsicHeight();
        intrinsicWidth = deleteDrawable.getIntrinsicWidth();

    }

    /**
     * Ustawiamy kierunek swipe'a za pomocą metody makeMovementFlags
     * @param recyclerView
     * @param viewHolder
     * @return kierunek swipe
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    /**
     * Metoda używana do przeciągania i upuszczania.
     * @param recyclerView
     * @param viewHolder
     * @param target
     * @return Nie używam więc zwracam false
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    /**
     * Metoda wywołuje po wykryciu akcji swipe
     * @param viewHolder
     * @param direction
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    /**
     * Niestandardowy widok, który pokazuje przesunięcie
     * @param c
     * @param recyclerView
     * @param viewHolder
     * @param dX
     * @param dY
     * @param actionState
     * @param isCurrentlyActive
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View viewItem = viewHolder.itemView;
        int itemHeight = viewItem.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        if (isCancelled) {
            clearCanvas(c, viewItem.getRight() + dX, viewItem.getTop(), viewItem.getRight(), viewItem.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        background.setColor(backgroundColor);
        background.setBounds((int) (viewItem.getRight() + dX), viewItem.getTop(), viewItem.getRight(), viewItem.getBottom());
        background.draw(c);

        int delIconTop = viewItem.getTop() + (itemHeight - intrinsicWidth) / 2;
        int delIconMargin = (itemHeight - intrinsicHeight) / 2;
        int delIconLeft = viewItem.getRight() - delIconMargin - intrinsicWidth;
        int delIconRight = viewItem.getRight() - delIconMargin;
        int delIconBottom = delIconTop + intrinsicHeight;

        deleteDrawable.setBounds(delIconLeft, delIconTop, delIconRight, delIconBottom);
        deleteDrawable.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, float left, float top, float right, float bottom) {
        c.drawRect(left, top, right, bottom, clearPaint);
    }

    /**
     * Metoda mówi nam ile % przesunięcia wymagane jest aby aktywować czynność swipe
     * @param viewHolder
     * @return
     */
    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.7f;
    }

}
