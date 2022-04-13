package com.ebe.miniaelec.ui.onlinereports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.ebe.miniaelec.R;


public class OnlineReportsFragment extends Fragment {

    WebView webView;
    String url;
    NavController navController;

    public OnlineReportsFragment() {
        // Required empty public constructor
    }






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        navController = Navigation.findNavController(requireActivity(),R.id.content);


        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // navController.popBackStack(R.id.mainFragment,false);
                //copyBackForwardList().getCurrentIndex() > 0
                if (webView.canGoBack()) {
                    webView.goBack();
                    //navController.navigateUp();
                } else requireActivity().getOnBackPressedDispatcher().onBackPressed();//navController.popBackStack(R.id.mainFragment,false);
            }
        });
        return inflater.inflate(R.layout.fragment_online_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = view.findViewById(R.id.web_view);
        url = "http://10.224.246.181:3000/";
        webView.setWebViewClient(new WebViewClient());
       // webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.88 Mobile Safari/537.36");
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.loadUrl(url);
    }


}