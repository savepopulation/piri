# piri
Piri is a lightweight annotation processing library that generates static factory methods which creates new intents for activities in Android.

## How to use? 

Add PiriActivity annotation to your Activity. 

```
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
                final Intent intent = Piri.newIntentForYourActivity(MainActivity.this);
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
                final Intent intent = Piri.newIntentForYourActivity(MainActivity.this,id,name);
                startActivity(intent);
            }
        });
...
}
```

## Baking..
Piri'll also support new instance method generation for Fragments soon. 

