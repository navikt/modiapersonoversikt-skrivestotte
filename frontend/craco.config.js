const path = require('path');
const CracoLessPlugin = require('craco-less');
const BUILD_PATH = path.resolve(__dirname, './build');

const RemoveCssHashPlugin = {
    overrideWebpackConfig: ({webpackConfig, cracoConfig, pluginOptions, context: {env, paths}}) => {
        const plugins = webpackConfig.plugins;
        plugins.forEach(plugin => {

            const options = plugin.options;

            if (!options) {
                return;
            }

            if (options.filename && options.filename.endsWith('.css')) {
                options.filename = "static/css/[name].css";
            }

        });

        return webpackConfig;
    }
};

const RemoveJsHashPlugin = {
    overrideCracoConfig: ({cracoConfig, pluginOptions, context: {env, paths}}) => {
        const webpack = cracoConfig.webpack || {};
        webpack.configure = {
            optimization: {
                splitChunks: {
                    cacheGroups: {
                        default: false,
                        vendors: false
                    },
                },
                runtimeChunk: false
            },
            output: {
                path: BUILD_PATH,
                filename: 'static/js/[name].js',
            },
        };
        return cracoConfig
    }
};

module.exports = {
    plugins: [
        {plugin: CracoLessPlugin},
        {plugin: RemoveCssHashPlugin},
        {plugin: RemoveJsHashPlugin}
    ]
};