import React, { Component } from 'react';
import { withStyles } from 'material-ui/styles';
import Toolbar from 'material-ui/Toolbar';
import Typography from 'material-ui/Typography';

const styles = theme => ({
  toolbar: {
    flexGrow: 1,
    flexDirection: 'column',
    alignItems: 'flex-start',
    justifyContent: 'center',
  },
  title: {
    color: theme.palette.text.secondary,
  },
  version: {
    paddingLeft: '5px',
  }
});

class Logo extends Component {
  render() {
    const { classes } = this.props;
    return (
      <Toolbar className={classes.toolbar}>
        <Typography type="title" color="inherit" className={classes.title}>
          GraphWalker
        </Typography>
        <Typography type="caption" className={classes.version}>
          4.0.0-SNAPSHOT
        </Typography>
      </Toolbar>
    );
  }
}

export default withStyles(styles, { withTheme: true })(Logo);
