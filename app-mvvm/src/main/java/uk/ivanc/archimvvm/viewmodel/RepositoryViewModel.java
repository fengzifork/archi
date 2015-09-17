package uk.ivanc.archimvvm.viewmodel;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.ivanc.archimvvm.ArchiApplication;
import uk.ivanc.archimvvm.R;
import uk.ivanc.archimvvm.model.GithubService;
import uk.ivanc.archimvvm.model.Repository;
import uk.ivanc.archimvvm.model.User;
import uk.ivanc.archimvvm.util.BindableFieldTarget;

/**
 * ViewModel for the RepositoryActivity
 */
public class RepositoryViewModel implements ViewModel {

    private static final String TAG = "RepositoryViewModel";

    private Repository repository;
    private Context context;
    private Subscription subscription;

    public ObservableField<String> ownerName;
    public ObservableField<String> ownerEmail;
    public ObservableField<String> ownerLocation;
    public ObservableInt ownerEmailVisibility;
    public ObservableInt ownerLocationVisibility;
    public ObservableInt ownerLayoutVisibility;
    public ObservableField<Drawable> ownerImage;

    public RepositoryViewModel(Context context, final Repository repository) {
        this.repository = repository;
        this.context = context;
        this.ownerName = new ObservableField<>();
        this.ownerEmail = new ObservableField<>();
        this.ownerLocation = new ObservableField<>();
        this.ownerLayoutVisibility = new ObservableInt(View.INVISIBLE);
        this.ownerEmailVisibility = new ObservableInt(View.VISIBLE);
        this.ownerLocationVisibility = new ObservableInt(View.VISIBLE);
        this.ownerImage = new ObservableField<>();
        //Trigger loading the rest of the user data as soon as the view model is created
        Picasso.with(context)
                .load(repository.owner.avatarUrl)
                .placeholder(R.drawable.placeholder)
                .into(new BindableFieldTarget(ownerImage, context.getResources()));
        loadFullUser(repository.owner.url);
    }

    public String getDescription() {
        return repository.description;
    }

    public String getHomepage() {
        return repository.homepage;
    }

    public int getHomepageVisibility() {
        return repository.hasHomepage() ? View.VISIBLE : View.GONE;
    }

    public String getLanguage() {
        return context.getString(R.string.text_language, repository.language);
    }

    public int getLanguageVisibility() {
        return repository.hasLanguage() ? View.VISIBLE : View.GONE;
    }

    public int getForkVisibility() {
        return repository.isFork() ? View.VISIBLE : View.GONE;
    }

    @Override
    public void destroy() {
        this.context = null;
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
    }

    private void loadFullUser(String url) {
        GithubService githubService = ArchiApplication.get(context).getGithubService();
        subscription = githubService.userFromUrl(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        Log.i(TAG, "Full user data loaded " + user);
                        ownerName.set(user.name);
                        ownerEmail.set(user.email);
                        ownerLocation.set(user.location);
                        ownerEmailVisibility.set(user.hasEmail() ? View.VISIBLE : View.GONE);
                        ownerLocationVisibility.set(user.hasLocation() ? View.VISIBLE : View.GONE);
                        ownerLayoutVisibility.set(View.VISIBLE);
                    }
                });
    }
}
