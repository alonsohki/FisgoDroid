package net.meneame.fisgodroid;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class SmileyPickerView extends LinearLayout
{
    public interface OnSmileySelectedListener
    {
        void onSmileySelected ( Smiley smiley );
    };
    
    private OnSmileySelectedListener mListener = null;
    private OnClickListener mButtonListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Object tag = v.getTag();
            if ( mListener != null && tag != null && tag instanceof Smiley )
            {
                Smiley smiley = (Smiley)tag;
                mListener.onSmileySelected(smiley);
            }
        }
    };
    
    public SmileyPickerView(Context context)
    {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.smiley_selector, this, true);
        
        if ( !isInEditMode() )
        {
            fillSmileys();
        }
    }

    public SmileyPickerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.smiley_selector, this, true);
        
        if ( !isInEditMode() )
        {
            fillSmileys();
        }
    }
    
    public void setOnSmileySelectedListener ( OnSmileySelectedListener listener )
    {
        mListener = listener;
    }
    
    private void fillSmileys ()
    {
        GridView grid = (GridView)findViewById(R.id.smileys_grid);
        Collection<Smiley> smileyCollection = Smileys.getSmileys();
        final Smiley[] smileys = new Smiley[smileyCollection.size()];
        smileyCollection.toArray(smileys);
        Arrays.sort(smileys, new Comparator<Smiley> () {
            @Override
            public int compare(Smiley arg0, Smiley arg1)
            {
                return arg0.getChatText().compareTo(arg1.getChatText());
            }
        });
        
        grid.setAdapter(new BaseAdapter () {
            @Override
            public int getCount()
            {
                return smileys.length;
            }

            @Override
            public Object getItem(int position)
            {
                return smileys[position];
            }

            @Override
            public long getItemId(int position)
            {
                 return getItem(position).hashCode();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                ImageButton button = (ImageButton)convertView;
                if ( button == null )
                {
                    button = new ImageButton(getContext());
                    button.setPadding(4, 4, 4, 4);
                    button.setOnClickListener(mButtonListener);
                }
                Smiley smiley = (Smiley)getItem(position);
                Drawable drawable = getResources().getDrawable(smiley.getResource());
                button.setBackgroundDrawable(drawable);
                button.setLayoutParams(new AbsListView.LayoutParams((int)(drawable.getIntrinsicWidth()*2.2),
                                                                    (int)(drawable.getIntrinsicHeight()*2.2)));
                button.setTag(smiley);
                return button;
            }

        });
    }
}
