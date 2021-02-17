package com.in;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@WebFilter(urlPatterns = "/*", filterName = "RecordFilter")
public class RequestRecordFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestRecordFilter.class);
    org.slf4j.Marker marker = org.slf4j.MarkerFactory.getMarker("REQUEST");

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        String method = request.getMethod();
        StringBuffer bufHeader = getHeader(request);
        StringBuffer bufParam = getParam(request);
        RequestWrapper wrappedRequest = new RequestWrapper(request);
        String body = getBody(wrappedRequest);
        wrappedRequest.resetInputStream();

        ResponseWrapper wrappedResponse = new ResponseWrapper(servletResponse);
        filterChain.doFilter(wrappedRequest, wrappedResponse);
        String responseData = getResponseData(wrappedResponse);
        LOGGER.error(marker,
                "uri=[{}],method=[{}],Header=[{}],Parameter=[{}],Body=[{}],response=[{}]",
                uri, method, bufHeader.toString(), bufParam.toString(), body, responseData);
    }

    private String getResponseData(ResponseWrapper wappedResponse)
            throws IOException {
        byte[] bytes = wappedResponse.getData();
        String result = new String(bytes, "UTF-8");
        wappedResponse.rewrite();
        return result;
    }

    private String getBody(RequestWrapper wrappedRequest) throws IOException {
        String body = IOUtils.toString(wrappedRequest.getReader());
        return body;
    }

    private StringBuffer getParam(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        StringBuffer bufParam = new StringBuffer();
        while (parameterNames.hasMoreElements()) {
            String paramKey = parameterNames.nextElement();
            String paramValue = request.getParameter(paramKey);
            bufParam.append(paramKey).append("=").append(paramValue).append(";");
        }
        return bufParam;
    }

    private StringBuffer getHeader(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuffer bufHeader = new StringBuffer();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            bufHeader.append(headerName).append("=").append(headerValue).append(";");
        }
        return bufHeader;
    }

    private static class RequestWrapper extends HttpServletRequestWrapper {

        private byte[] rawData;
        private HttpServletRequest request;
        private ResettableServletInputStream servletStream;

        public RequestWrapper(HttpServletRequest request) {
            super(request);
            this.request = request;
            this.servletStream = new ResettableServletInputStream();
        }


        public void resetInputStream() {
            servletStream.stream = new ByteArrayInputStream(rawData);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return servletStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return new BufferedReader(new InputStreamReader(servletStream));
        }


        private class ResettableServletInputStream extends ServletInputStream {
            private InputStream stream;

            @Override
            public int read() throws IOException {
                return stream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        }
    }

    private static class ResponseWrapper extends HttpServletResponseWrapper {

        private ByteArrayOutputStream bos = new ByteArrayOutputStream();
        private ServletResponse response;
        private PrintWriter writer;
        private byte[] data;

        public ResponseWrapper(ServletResponse response) {
            super((HttpServletResponse) response);
            this.response = response;
        }

        @Override
        public ServletOutputStream getOutputStream() {
            return new MyServletOutputStream(bos);
        }

        @Override
        public PrintWriter getWriter() throws UnsupportedEncodingException {
            writer = new PrintWriter(new OutputStreamWriter(bos, "utf-8"));
            return writer;
        }

        public byte[] getData() throws IOException {
            bos.flush();
            data = this.bos.toByteArray();
            return data;
        }

        public void rewrite() {
            try (ServletOutputStream outputStream = response.getOutputStream();) {
                outputStream.write(data);
                outputStream.flush();
            } catch (IOException e) {
                LOGGER.warn("rewrite error:", e);
            }
        }

        class MyServletOutputStream extends ServletOutputStream {

            private ByteArrayOutputStream ostream;

            public MyServletOutputStream(ByteArrayOutputStream ostream) {
                this.ostream = ostream;
            }

            @Override
            public void write(int b) throws IOException {
                ostream.write(b);
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener listener) {

            }
        }

    }

    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info("init RecordFilter");
    }

    public void destroy() {
        // Nothing to do
    }

}
