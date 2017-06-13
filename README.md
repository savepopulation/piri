# piri
Piri is a lightweight annotation processing library that generates static factory methods for your Activities and Fragments.

## How to use? 

Add PiriActivity annotation to your Activity. 

```java
@PiriActivity
public class YourActivity extends AppCompatActivity {
...
}
```

And start YourActivity from another Activity.

```java
public class MainActivity extends AppCompatActivity {
...
 navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = PiriIntentFactory.newIntentForYourActivity(MainActivity.this);
                startActivity(intent);
            }
        });
...
}
```

If you want to pass data to YourActivity with Piri add PiriParam annotation to your fields in your Activity and receive data from bundle.

```java
@PiriActivity
public class YourActivity extends AppCompatActivity {
    
    @PiriParam(key = "extra_id")
    private Long id;

    @PiriParam(key = "extra_name")
    private String name;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your);

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getLong("extra_id");
            name = bundle.getString("extra_name");
        }

        // INIT UI
        ...
    }
}
```

And start YourActivity like the following:

```java
public class MainActivity extends AppCompatActivity {
...

 final Long id = 1234567890L;
 final String name = "PiriExample";
 navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = PiriIntentFactory.newIntentForYourActivity(MainActivity.this,id,name);
                startActivity(intent);
            }
        });
...
}
```

##Now you can generate your fragment instances with Piri.

Add PiriFragment annotation to your Fragments.

```java
@PiriFragment
public class SampleFragment extends Fragment{
    
    private static final String EXTRA_ID = "extra_id";
    private static final String EXTRA_USER = "extra_user";
    private static final String EXTRA_BOOK = "extra_book";

    @PiriParam(key = EXTRA_ID)
    private Long id;

    @PiriParam(key = EXTRA_USER)
    private User user;

    @PiriParam(key = EXTRA_BOOK)
    private Book book;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args != null) {
            id = args.getLong(EXTRA_ID, 0);
            user = args.getParcelable(EXTRA_USER);
            book = (Book) args.getSerializable(EXTRA_BOOK);
        }
    }
}
```

<b>Arrays not supported for Fragment Instance generation for now.</b>

## Where Piri comes from?
https://en.wikipedia.org/wiki/P%C3%AEr%C3%AE_Reis



